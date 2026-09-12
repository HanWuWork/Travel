package com.example.travelserver.service.dest.impl;

import com.example.travelserver.common.BusinessException;
import com.example.travelserver.entity.Checkin;
import com.example.travelserver.entity.City;
import com.example.travelserver.repository.CheckinRepository;
import com.example.travelserver.repository.CityRepository;
import com.example.travelserver.service.dest.CheckinService;
import com.example.travelserver.vo.dest.CheckinStatsVO;
import com.example.travelserver.vo.dest.CheckinVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CheckinServiceImpl implements CheckinService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Set<String> VALID_STATUS = Set.of(Checkin.VISITED, Checkin.WISH, Checkin.PLANNED);

    private final CheckinRepository checkinRepository;
    private final CityRepository cityRepository;

    public CheckinServiceImpl(CheckinRepository checkinRepository, CityRepository cityRepository) {
        this.checkinRepository = checkinRepository;
        this.cityRepository = cityRepository;
    }

    @Override
    public List<CheckinVO> listByUser(Long userId) {
        List<CheckinVO> result = new ArrayList<>();
        for (Checkin c : checkinRepository.findByUserIdOrderByUpdateTimeDesc(userId)) {
            cityRepository.findById(c.getCityId()).ifPresent(city -> result.add(toVO(c, city)));
        }
        return result;
    }

    @Override
    @Transactional
    public CheckinVO mark(Long userId, Long cityId, String status, String note) {
        if (!VALID_STATUS.contains(status)) {
            throw new BusinessException(400, "无效的打卡状态");
        }
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new BusinessException(404, "城市不存在"));

        Checkin checkin = checkinRepository.findByUserIdAndCityId(userId, cityId)
                .orElseGet(() -> {
                    Checkin c = new Checkin();
                    c.setUserId(userId);
                    c.setCityId(cityId);
                    c.setCreateTime(LocalDateTime.now());
                    c.setVisitCount(0);
                    return c;
                });

        // 首次标记为去过时次数 +1；从其他状态切换到 visited 也 +1
        if (Checkin.VISITED.equals(status) && !Checkin.VISITED.equals(checkin.getStatus())) {
            checkin.setVisitCount((checkin.getVisitCount() == null ? 0 : checkin.getVisitCount()) + 1);
        }
        checkin.setStatus(status);
        if (note != null) {
            checkin.setNote(note);
        }
        checkin.setUpdateTime(LocalDateTime.now());
        checkin = checkinRepository.save(checkin);
        return toVO(checkin, city);
    }

    @Override
    @Transactional
    public void remove(Long userId, Long cityId) {
        checkinRepository.findByUserIdAndCityId(userId, cityId)
                .ifPresent(checkinRepository::delete);
    }

    @Override
    public CheckinStatsVO stats(Long userId) {
        CheckinStatsVO vo = new CheckinStatsVO();
        vo.setVisitedCount(checkinRepository.countByUserIdAndStatus(userId, Checkin.VISITED));
        vo.setWishCount(checkinRepository.countByUserIdAndStatus(userId, Checkin.WISH));
        vo.setPlannedCount(checkinRepository.countByUserIdAndStatus(userId, Checkin.PLANNED));

        Set<String> provinces = new HashSet<>();
        for (Checkin c : checkinRepository.findByUserIdOrderByUpdateTimeDesc(userId)) {
            if (Checkin.VISITED.equals(c.getStatus())) {
                cityRepository.findById(c.getCityId()).ifPresent(city -> provinces.add(city.getProvince()));
            }
        }
        vo.setVisitedProvinces(new ArrayList<>(provinces));
        return vo;
    }

    private CheckinVO toVO(Checkin c, City city) {
        CheckinVO vo = new CheckinVO();
        vo.setId(c.getId());
        vo.setCityId(city.getId());
        vo.setCityName(city.getName());
        vo.setProvince(city.getProvince());
        vo.setLatitude(city.getLatitude());
        vo.setLongitude(city.getLongitude());
        vo.setStatus(c.getStatus());
        vo.setNote(c.getNote());
        vo.setVisitCount(c.getVisitCount());
        vo.setUpdateTime(c.getUpdateTime() == null ? null : c.getUpdateTime().format(TIME_FMT));
        return vo;
    }
}
