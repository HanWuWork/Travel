package com.example.travelserver.service.dest;

import com.example.travelserver.vo.dest.CheckinStatsVO;
import com.example.travelserver.vo.dest.CheckinVO;

import java.util.List;

/**
 * 城市足迹服务：去过/想去/计划中 三态打卡
 */
public interface CheckinService {

    /** 我的足迹列表 */
    List<CheckinVO> listByUser(Long userId);

    /** 打卡/更新状态（同一城市仅保留一个状态；再次打卡 visited 会累加次数） */
    CheckinVO mark(Long userId, Long cityId, String status, String note);

    /** 取消打卡 */
    void remove(Long userId, Long cityId);

    /** 足迹统计 */
    CheckinStatsVO stats(Long userId);
}
