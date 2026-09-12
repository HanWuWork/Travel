package com.example.travelserver.service.dest.impl;

import com.example.travelserver.common.BusinessException;
import com.example.travelserver.entity.Attraction;
import com.example.travelserver.entity.City;
import com.example.travelserver.repository.AttractionRepository;
import com.example.travelserver.repository.CityRepository;
import com.example.travelserver.service.dest.DestinationService;
import com.example.travelserver.vo.dest.AttractionVO;
import com.example.travelserver.vo.dest.CityVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DestinationServiceImpl implements DestinationService {

    private static final Map<String, String> TYPE_LABELS = Map.of(
            "attraction", "景点",
            "food", "美食",
            "hotel", "住宿"
    );

    private final CityRepository cityRepository;
    private final AttractionRepository attractionRepository;

    public DestinationServiceImpl(CityRepository cityRepository, AttractionRepository attractionRepository) {
        this.cityRepository = cityRepository;
        this.attractionRepository = attractionRepository;
    }

    @Override
    public List<CityVO> listCities() {
        List<City> cities = cityRepository.findAllByOrderByIdAsc();
        return cities.stream()
                .sorted(Comparator.comparing(City::getHot).reversed())
                .map(this::toCityVO)
                .collect(Collectors.toList());
    }

    @Override
    public CityVO getCity(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "城市不存在"));
        return toCityVO(city);
    }

    @Override
    public List<AttractionVO> listAttractions(Long cityId, String type, String keyword) {
        if (cityId == null) {
            throw new BusinessException(400, "请指定城市");
        }
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new BusinessException(404, "城市不存在"));

        List<Attraction> list = (type == null || type.isBlank() || "all".equals(type))
                ? attractionRepository.findByCityIdOrderByIdAsc(cityId)
                : attractionRepository.findByCityIdAndTypeOrderByIdAsc(cityId, type);

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            list = list.stream()
                    .filter(a -> a.getName().contains(kw)
                            || (a.getDescription() != null && a.getDescription().contains(kw)))
                    .collect(Collectors.toList());
        }

        return list.stream().map(a -> toAttractionVO(a, city.getName())).collect(Collectors.toList());
    }

    @Override
    public AttractionVO getAttraction(Long id) {
        Attraction attraction = attractionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "地点不存在"));
        String cityName = cityRepository.findById(attraction.getCityId())
                .map(City::getName)
                .orElse("");
        return toAttractionVO(attraction, cityName);
    }

    @Override
    public List<AttractionVO> nearby(double lat, double lng, double radiusKm, String type) {
        List<Attraction> all = attractionRepository.findAll();
        if (type != null && !type.isBlank() && !"all".equals(type)) {
            all = all.stream().filter(a -> type.equals(a.getType())).collect(java.util.stream.Collectors.toList());
        }

        record Scored(Attraction a, double dist) {}
        List<Scored> scored = new ArrayList<>();
        for (Attraction a : all) {
            double d = haversineKm(lat, lng, a.getLatitude(), a.getLongitude());
            if (d <= radiusKm) {
                scored.add(new Scored(a, d));
            }
        }
        scored.sort(java.util.Comparator.comparingDouble(Scored::dist));

        return scored.stream()
                .limit(50)
                .map(s -> {
                    AttractionVO vo = toAttractionVO(s.a(), "");
                    vo.setDistanceKm(Math.round(s.dist() * 10) / 10.0);
                    return vo;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<AttractionVO> hotSpots(int limit) {
        int n = Math.max(1, Math.min(limit, 20));
        Map<Long, String> cityNames = cityRepository.findAll().stream()
                .collect(Collectors.toMap(City::getId, City::getName, (a, b) -> a));
        return attractionRepository.findAll().stream()
                .filter(a -> "attraction".equals(a.getType()))
                .sorted(Comparator.comparingDouble((Attraction a) -> a.getRating() == null ? 0 : a.getRating())
                        .reversed()
                        .thenComparing(Attraction::getId))
                .limit(n)
                .map(a -> toAttractionVO(a, cityNames.getOrDefault(a.getCityId(), "")))
                .collect(Collectors.toList());
    }

    /** Haversine 公式计算两点球面距离（公里） */
    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * r * Math.asin(Math.sqrt(h));
    }

    private CityVO toCityVO(City city) {
        CityVO vo = new CityVO();
        vo.setId(city.getId());
        vo.setName(city.getName());
        vo.setProvince(city.getProvince());
        vo.setDescription(city.getDescription());
        vo.setLatitude(city.getLatitude());
        vo.setLongitude(city.getLongitude());
        vo.setHot(city.getHot());
        vo.setSpotCount(attractionRepository.countByCityId(city.getId()));
        return vo;
    }

    private AttractionVO toAttractionVO(Attraction a, String cityName) {
        AttractionVO vo = new AttractionVO();
        vo.setId(a.getId());
        vo.setCityId(a.getCityId());
        vo.setCityName(cityName);
        vo.setName(a.getName());
        vo.setType(a.getType());
        vo.setTypeLabel(TYPE_LABELS.getOrDefault(a.getType(), a.getType()));
        vo.setDescription(a.getDescription());
        vo.setLatitude(a.getLatitude());
        vo.setLongitude(a.getLongitude());
        vo.setTicket(a.getTicket());
        vo.setOpenTime(a.getOpenTime());
        vo.setPlayTime(a.getPlayTime());
        vo.setRating(a.getRating());
        return vo;
    }
}
