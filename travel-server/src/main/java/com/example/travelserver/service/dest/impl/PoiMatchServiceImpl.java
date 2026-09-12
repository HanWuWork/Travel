package com.example.travelserver.service.dest.impl;

import com.example.travelserver.entity.Attraction;
import com.example.travelserver.entity.City;
import com.example.travelserver.repository.AttractionRepository;
import com.example.travelserver.repository.CityRepository;
import com.example.travelserver.service.dest.PoiMatchService;
import com.example.travelserver.vo.travel.PlanPoiVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 行程 POI 坐标匹配实现：
 * AI 输出的地点名往往带修饰词（如“西湖风景名胜区（苏堤）”），采用包含/被包含关系做双向模糊匹配，
 * 并按名称长度差最小者优先，保证“故宫”能命中“故宫博物院”、“兵马俑”能命中“秦始皇兵马俑博物馆”。
 */
@Service
public class PoiMatchServiceImpl implements PoiMatchService {

    private final CityRepository cityRepository;
    private final AttractionRepository attractionRepository;

    public PoiMatchServiceImpl(CityRepository cityRepository, AttractionRepository attractionRepository) {
        this.cityRepository = cityRepository;
        this.attractionRepository = attractionRepository;
    }

    @Override
    public Long matchCityId(String destination) {
        if (destination == null || destination.isBlank()) {
            return null;
        }
        String dest = clean(destination);
        // 1. 精确/包含匹配城市名：如 “杭州”“杭州市”“浙江杭州”
        Optional<City> exact = cityRepository.findAll().stream()
                .filter(c -> {
                    String name = clean(c.getName());
                    return dest.equals(name) || dest.contains(name) || name.contains(dest);
                })
                .min(Comparator.comparingInt(c -> Math.abs(clean(c.getName()).length() - dest.length())));
        if (exact.isPresent()) {
            return exact.get().getId();
        }
        return null;
    }

    @Override
    public List<PlanPoiVO> matchPois(Long cityId, List<String> names) {
        List<Attraction> candidates = cityId != null
                ? attractionRepository.findByCityIdOrderByIdAsc(cityId)
                : attractionRepository.findAll();

        List<PlanPoiVO> result = new ArrayList<>();
        int seq = 1;
        for (String raw : names) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String target = clean(raw);
            if (target.length() < 2) {
                continue;
            }
            Attraction best = null;
            int bestGap = Integer.MAX_VALUE;
            for (Attraction a : candidates) {
                String name = clean(a.getName());
                if (name.equals(target)) {
                    best = a;
                    bestGap = 0;
                    break;
                }
                boolean contains = name.contains(target) || target.contains(name);
                // 名称过短时要求正向包含，避免“大雁塔”误命中“大雁塔北广场”以外的噪声
                boolean ok = contains && (target.length() >= 3 || name.contains(target));
                if (ok) {
                    int gap = Math.abs(name.length() - target.length());
                    if (gap < bestGap) {
                        best = a;
                        bestGap = gap;
                    }
                }
            }
            if (best != null) {
                result.add(new PlanPoiVO(best.getId(), best.getName(), 0,
                        best.getLatitude(), best.getLongitude(), seq++));
            }
        }
        return result;
    }

    /** 去掉常见修饰符与空白，便于比较 */
    private String clean(String s) {
        return s.replaceAll("[\\s（）()【】\\[\\]·、，,。.!！？?~～]", "")
                .replace("市", "")
                .trim();
    }

    @Override
    public List<PlanPoiVO> topPois(String destination, int limit) {
        Long cityId = matchCityId(destination);
        if (cityId == null) {
            return List.of();
        }
        List<Attraction> top = attractionRepository.findByCityIdOrderByIdAsc(cityId).stream()
                .filter(a -> "attraction".equals(a.getType()))
                .sorted(Comparator.comparingDouble((Attraction a) -> a.getRating() == null ? 0 : a.getRating()).reversed())
                .limit(Math.max(1, limit))
                .collect(Collectors.toList());

        List<PlanPoiVO> result = new ArrayList<>();
        int seq = 1;
        for (Attraction a : top) {
            result.add(new PlanPoiVO(a.getId(), a.getName(), 0, a.getLatitude(), a.getLongitude(), seq++));
        }
        return result;
    }
}
