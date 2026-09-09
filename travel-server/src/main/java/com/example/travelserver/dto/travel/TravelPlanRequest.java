package com.example.travelserver.dto.travel;

/**
 * 旅游规划请求
 */
public class TravelPlanRequest {

    /** 旅游目的地 */
    private String destination;

    /** 预算金额（元） */
    private Integer budget;

    /** 行程天数 */
    private Integer days;

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Integer getBudget() {
        return budget;
    }

    public void setBudget(Integer budget) {
        this.budget = budget;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }
}
