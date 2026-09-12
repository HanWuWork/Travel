package com.example.travelserver.service.dest.impl;

import com.example.travelserver.entity.Attraction;
import com.example.travelserver.entity.Checkin;
import com.example.travelserver.entity.City;
import com.example.travelserver.entity.Favorite;
import com.example.travelserver.entity.Trip;
import com.example.travelserver.repository.AttractionRepository;
import com.example.travelserver.repository.CheckinRepository;
import com.example.travelserver.repository.CityRepository;
import com.example.travelserver.repository.FavoriteRepository;
import com.example.travelserver.repository.TripRepository;
import com.example.travelserver.service.dest.RecommendService;
import com.example.travelserver.vo.dest.AttractionVO;
import com.example.travelserver.vo.dest.RecommendVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 个性化推荐实现（规则算法，无需外部服务）：
 * 1. 汇总用户信号：收藏的景点 → 对应城市；已保存行程目的地；足迹（去过/想去/计划）；
 * 2. 用「收藏景点文本」与「候选城市及其景点文本」做中文二元组（bigram）重合度打分；
 * 3. 叠加热门加成，输出未去过城市的推荐及理由；
 * 4. 同时在收藏城市中推荐尚未收藏的高分景点。
 * 无任何用户数据时降级为热门城市推荐。
 */
@Service
public class RecommendServiceImpl implements RecommendService {

    private static final int CITY_LIMIT = 6;
    private static final int SPOT_LIMIT = 8;

    private final FavoriteRepository favoriteRepository;
    private final TripRepository tripRepository;
    private final CheckinRepository checkinRepository;
    private final CityRepository cityRepository;
    private final AttractionRepository attractionRepository;

    public RecommendServiceImpl(FavoriteRepository favoriteRepository,
                                TripRepository tripRepository,
                                CheckinRepository checkinRepository,
                                CityRepository cityRepository,
                                AttractionRepository attractionRepository) {
        this.favoriteRepository = favoriteRepository;
        this.tripRepository = tripRepository;
        this.checkinRepository = checkinRepository;
        this.cityRepository = cityRepository;
        this.attractionRepository = attractionRepository;
    }

    @Override
    public RecommendVO recommend(Long userId) {
        List<City> allCities = cityRepository.findAllByOrderByIdAsc();
        Map<Long, City> cityById = allCities.stream().collect(Collectors.toMap(City::getId, c -> c));
        Map<Long, List<Attraction>> spotsByCity = new HashMap<>();
        for (Attraction a : attractionRepository.findAll()) {
            spotsByCity.computeIfAbsent(a.getCityId(), k -> new ArrayList<>()).add(a);
        }

        // ===== 汇总用户信号 =====
        Set<Long> favoredSpotIds = new HashSet<>();
        StringBuilder profile = new StringBuilder();
        if (userId != null) {
            for (Favorite f : favoriteRepository.findByUserIdOrderByCreateTimeDesc(userId)) {
                if ("spot".equals(f.getTargetType()) && f.getTargetId() != null && f.getTargetId() > 0) {
                    favoredSpotIds.add(f.getTargetId());
                }
                if (f.getTitle() != null) {
                    profile.append(f.getTitle()).append(' ');
                }
                if (f.getDescription() != null) {
                    profile.append(f.getDescription()).append(' ');
                }
            }
        }
        // 收藏景点 → 城市 + 扩充画像文本
        Set<Long> favoredCityIds = new HashSet<>();
        Map<Long, String> topFavSpotByCity = new LinkedHashMap<>();
        for (Attraction a : attractionRepository.findAllById(favoredSpotIds)) {
            favoredCityIds.add(a.getCityId());
            profile.append(a.getName()).append(' ').append(a.getDescription() == null ? "" : a.getDescription()).append(' ');
            topFavSpotByCity.putIfAbsent(a.getCityId(), a.getName());
        }

        // 足迹 + 行程目的地 → 已去过/计划中的城市
        Set<Long> touchedCityIds = new HashSet<>(favoredCityIds);
        if (userId != null) {
            for (Checkin c : checkinRepository.findByUserIdOrderByUpdateTimeDesc(userId)) {
                touchedCityIds.add(c.getCityId());
            }
            for (Trip t : tripRepository.findByUserIdOrderByCreateTimeDesc(userId)) {
                City city = matchCity(t.getDestination(), allCities);
                if (city != null) {
                    touchedCityIds.add(city.getId());
                    profile.append(city.getName()).append(' ');
                }
            }
        }

        boolean personalized = profile.length() > 0;
        Map<String, Integer> profileGrams = bigrams(profile.toString());

        // ===== 城市推荐 =====
        List<RecommendVO.CityReco> recos = new ArrayList<>();
        for (City city : allCities) {
            if (touchedCityIds.contains(city.getId())) {
                continue;
            }
            List<Attraction> spots = spotsByCity.getOrDefault(city.getId(), List.of());
            StringBuilder cityText = new StringBuilder(city.getName()).append(' ').append(city.getDescription() == null ? "" : city.getDescription());
            for (Attraction a : spots) {
                cityText.append(a.getName()).append(' ');
                if (a.getDescription() != null) {
                    cityText.append(a.getDescription()).append(' ');
                }
            }
            double overlap = overlapScore(profileGrams, bigrams(cityText.toString()));
            double score = overlap * 10 + (Boolean.TRUE.equals(city.getHot()) ? 1.5 : 0);

            RecommendVO.CityReco reco = new RecommendVO.CityReco();
            reco.setId(city.getId());
            reco.setName(city.getName());
            reco.setProvince(city.getProvince());
            reco.setDescription(city.getDescription());
            reco.setHot(city.getHot());
            reco.setScore(Math.round(score * 100) / 100.0);
            reco.setReason(buildReason(city, spots, profileGrams, personalized));
            recos.add(reco);
        }
        recos.sort(Comparator.comparingDouble(RecommendVO.CityReco::getScore).reversed());
        if (recos.size() > CITY_LIMIT) {
            recos = new ArrayList<>(recos.subList(0, CITY_LIMIT));
        }

        // ===== 景点推荐（收藏城市内未收藏的高分景点） =====
        List<AttractionVO> spotRecos = new ArrayList<>();
        for (Long cityId : favoredCityIds) {
            City city = cityById.get(cityId);
            if (city == null) {
                continue;
            }
            List<Attraction> candidates = spotsByCity.getOrDefault(cityId, List.of()).stream()
                    .filter(a -> !favoredSpotIds.contains(a.getId()))
                    .filter(a -> "attraction".equals(a.getType()))
                    .sorted(Comparator.comparingDouble((Attraction a) -> a.getRating() == null ? 0 : a.getRating()).reversed())
                    .limit(3)
                    .collect(Collectors.toList());
            for (Attraction a : candidates) {
                spotRecos.add(toSpotVO(a, city.getName()));
            }
        }
        if (spotRecos.size() > SPOT_LIMIT) {
            spotRecos = new ArrayList<>(spotRecos.subList(0, SPOT_LIMIT));
        }

        RecommendVO vo = new RecommendVO();
        vo.setCities(recos);
        vo.setAttractions(spotRecos);
        vo.setPersonalized(personalized);
        vo.setBasis(personalized ? "根据你的收藏、行程与足迹推荐" : "热门目的地推荐");
        return vo;
    }

    /** 生成推荐理由：优先引用同省/收藏过的城市，其次引用风格相近的收藏景点，最后回退热门 */
    private String buildReason(City city, List<Attraction> spots, Map<String, Integer> profileGrams, boolean personalized) {
        if (!personalized) {
            return "热门目的地，值得一去";
        }
        // 与城市景点文本最相似的收藏关键词
        String best = null;
        int bestScore = 0;
        for (Attraction a : spots) {
            int s = overlapCount(profileGrams, bigrams(a.getName() + " " + (a.getDescription() == null ? "" : a.getDescription())));
            if (s > bestScore) {
                bestScore = s;
                best = a.getName();
            }
        }
        if (best != null && bestScore >= 3) {
            return "和你喜欢的「" + best + "」风格相近";
        }
        return Boolean.TRUE.equals(city.getHot()) ? "热门目的地，符合你的旅行口味" : "为你发现的小众目的地";
    }

    private City matchCity(String destination, List<City> cities) {
        if (destination == null || destination.isBlank()) {
            return null;
        }
        String dest = destination.replace("市", "").trim();
        return cities.stream()
                .filter(c -> dest.contains(c.getName()) || c.getName().contains(dest))
                .findFirst()
                .orElse(null);
    }

    private AttractionVO toSpotVO(Attraction a, String cityName) {
        AttractionVO vo = new AttractionVO();
        vo.setId(a.getId());
        vo.setCityId(a.getCityId());
        vo.setCityName(cityName);
        vo.setName(a.getName());
        vo.setType(a.getType());
        vo.setTypeLabel("景点");
        vo.setDescription(a.getDescription());
        vo.setLatitude(a.getLatitude());
        vo.setLongitude(a.getLongitude());
        vo.setTicket(a.getTicket());
        vo.setOpenTime(a.getOpenTime());
        vo.setPlayTime(a.getPlayTime());
        vo.setRating(a.getRating());
        return vo;
    }

    /** 中文文本二元组集合 */
    private Map<String, Integer> bigrams(String text) {
        Map<String, Integer> grams = new HashMap<>();
        if (text == null) {
            return grams;
        }
        String t = text.replaceAll("[\\s\\p{Punct}，。、；：！？（）【】「」《》]", "");
        for (int i = 0; i + 1 < t.length(); i++) {
            String g = t.substring(i, i + 2);
            grams.merge(g, 1, Integer::sum);
        }
        return grams;
    }

    /** 重合度（0-1）：重合二元组数 / 城市二元组数，做长度归一避免长文本占优 */
    private double overlapScore(Map<String, Integer> profile, Map<String, Integer> target) {
        if (profile.isEmpty() || target.isEmpty()) {
            return 0;
        }
        long hit = 0;
        for (String g : target.keySet()) {
            if (profile.containsKey(g)) {
                hit++;
            }
        }
        return (double) hit / target.size();
    }

    private int overlapCount(Map<String, Integer> profile, Map<String, Integer> target) {
        int hit = 0;
        for (String g : target.keySet()) {
            if (profile.containsKey(g)) {
                hit++;
            }
        }
        return hit;
    }
}
