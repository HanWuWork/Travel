package com.example.travelserver.dto.travel;

import com.example.travelserver.vo.travel.TravelPlanVO;

/**
 * 行程保存请求
 */
public class TripSaveRequest {

    /** 出发日期（yyyy-MM-dd，可空） */
    private String startDate;

    /** 行程数据 */
    private TravelPlanVO plan;

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public TravelPlanVO getPlan() { return plan; }
    public void setPlan(TravelPlanVO plan) { this.plan = plan; }
}
