package com.example.travelserver.vo.dest;

import lombok.Data;

import java.util.List;

/**
 * 足迹统计 VO
 */
@Data
public class CheckinStatsVO {

    private long visitedCount;
    private long wishCount;
    private long plannedCount;

    /** 去过城市的省份列表（足迹覆盖省份） */
    private List<String> visitedProvinces;
}
