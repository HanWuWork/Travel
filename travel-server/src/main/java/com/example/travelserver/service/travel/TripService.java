package com.example.travelserver.service.travel;

import com.example.travelserver.vo.travel.TravelPlanVO;

import java.util.List;

/**
 * 行程持久化服务：保存/查询/删除 AI 生成的行程
 */
public interface TripService {

    /** 保存行程，返回带 ID 的行程数据 */
    TravelPlanVO save(Long userId, String startDate, TravelPlanVO plan);

    /** 更新已有行程（微调后保存） */
    TravelPlanVO update(Long userId, Long tripId, String startDate, TravelPlanVO plan);

    /** 当前用户的行程列表（含完整行程 JSON） */
    List<TravelPlanVO> listByUser(Long userId);

    /** 查询单个行程 */
    TravelPlanVO get(Long userId, Long tripId);

    /** 删除行程 */
    void delete(Long userId, Long tripId);
}
