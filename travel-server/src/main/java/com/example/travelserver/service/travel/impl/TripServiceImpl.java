package com.example.travelserver.service.travel.impl;

import com.example.travelserver.common.BusinessException;
import com.example.travelserver.entity.Trip;
import com.example.travelserver.repository.TripRepository;
import com.example.travelserver.service.travel.TripService;
import com.example.travelserver.vo.travel.DayPlanVO;
import com.example.travelserver.vo.travel.TravelPlanVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TripServiceImpl implements TripService {

    private static final Logger log = LoggerFactory.getLogger(TripServiceImpl.class);

    private final TripRepository tripRepository;
    private final ObjectMapper objectMapper;

    public TripServiceImpl(TripRepository tripRepository, ObjectMapper objectMapper) {
        this.tripRepository = tripRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public TravelPlanVO save(Long userId, String startDate, TravelPlanVO plan) {
        if (plan == null || plan.getDestination() == null || plan.getDestination().isBlank()) {
            throw new BusinessException(400, "行程数据不完整，无法保存");
        }
        Trip trip = new Trip();
        trip.setUserId(userId);
        trip.setDestination(plan.getDestination());
        trip.setBudget(plan.getBudget());
        trip.setDays(plan.getDays());
        trip.setStartDate(startDate);
        trip.setCreateTime(LocalDateTime.now());
        trip.setUpdateTime(LocalDateTime.now());
        trip.setVersion(1);
        try {
            trip.setPlanJson(objectMapper.writeValueAsString(plan));
        } catch (Exception e) {
            log.error("行程序列化失败", e);
            throw new BusinessException(500, "行程保存失败");
        }
        trip = tripRepository.save(trip);
        plan.setId(trip.getId());
        plan.setStartDate(trip.getStartDate());
        return plan;
    }

    @Override
    public TravelPlanVO update(Long userId, Long tripId, String startDate, TravelPlanVO plan) {
        if (plan == null || plan.getDestination() == null || plan.getDestination().isBlank()) {
            throw new BusinessException(400, "行程数据不完整，无法保存");
        }
        Trip trip = mustGet(userId, tripId);
        trip.setDestination(plan.getDestination());
        trip.setBudget(plan.getBudget());
        trip.setDays(plan.getDays());
        trip.setUpdateTime(LocalDateTime.now());
        if (startDate != null) {
            trip.setStartDate(startDate.isBlank() ? trip.getStartDate() : startDate);
        }
        try {
            trip.setPlanJson(objectMapper.writeValueAsString(plan));
        } catch (Exception e) {
            log.error("行程序列化失败", e);
            throw new BusinessException(500, "行程保存失败");
        }
        trip = tripRepository.save(trip);
        plan.setId(trip.getId());
        plan.setStartDate(trip.getStartDate());
        return plan;
    }

    @Override
    public List<TravelPlanVO> listByUser(Long userId) {
        List<TravelPlanVO> result = new ArrayList<>();
        for (Trip trip : tripRepository.findByUserIdOrderByCreateTimeDesc(userId)) {
            result.add(summary(fromEntity(trip)));
        }
        return result;
    }

    /** 列表场景裁剪：只保留列表页需要的字段，去掉每日详情/预算/提示等，减少序列化与传输成本 */
    private TravelPlanVO summary(TravelPlanVO vo) {
        if (vo == null) {
            return null;
        }
        if (vo.getItinerary() != null) {
            for (DayPlanVO day : vo.getItinerary()) {
                day.setDescription(null);
                day.setTip(null);
            }
        }
        vo.setBudgetBreakdown(null);
        vo.setTips(null);
        return vo;
    }

    @Override
    public TravelPlanVO get(Long userId, Long tripId) {
        return fromEntity(mustGet(userId, tripId));
    }

    @Override
    public void delete(Long userId, Long tripId) {
        Trip trip = mustGet(userId, tripId);
        tripRepository.delete(trip);
    }

    private Trip mustGet(Long userId, Long tripId) {
        return tripRepository.findByIdAndUserId(tripId, userId)
                .orElseThrow(() -> new BusinessException(404, "行程不存在"));
    }

    private TravelPlanVO fromEntity(Trip trip) {
        try {
            TravelPlanVO vo = objectMapper.readValue(trip.getPlanJson(), TravelPlanVO.class);
            vo.setId(trip.getId());
            vo.setStartDate(trip.getStartDate());
            vo.setDestination(trip.getDestination());
            vo.setBudget(trip.getBudget());
            vo.setDays(trip.getDays());
            return vo;
        } catch (Exception e) {
            log.warn("行程 JSON 反序列化失败，返回基础信息: {}", e.getMessage());
            TravelPlanVO vo = new TravelPlanVO();
            vo.setId(trip.getId());
            vo.setStartDate(trip.getStartDate());
            vo.setDestination(trip.getDestination());
            vo.setBudget(trip.getBudget());
            vo.setDays(trip.getDays());
            vo.setItinerary(List.of());
            vo.setBudgetBreakdown(List.of());
            vo.setTips(List.of());
            return vo;
        }
    }
}
