package com.example.travelserver.vo.travel;

import java.util.List;

/**
 * 旅游规划结果
 */
public class TravelPlanVO {

    /** 目的地 */
    private String destination;

    /** 预算（元） */
    private Integer budget;

    /** 天数 */
    private Integer days;

    /** 每日行程 */
    private List<DayPlanVO> itinerary;

    /** 预算分配 */
    private List<BudgetItemVO> budgetBreakdown;

    /** 温馨提示 */
    private List<String> tips;

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

    public List<DayPlanVO> getItinerary() {
        return itinerary;
    }

    public void setItinerary(List<DayPlanVO> itinerary) {
        this.itinerary = itinerary;
    }

    public List<BudgetItemVO> getBudgetBreakdown() {
        return budgetBreakdown;
    }

    public void setBudgetBreakdown(List<BudgetItemVO> budgetBreakdown) {
        this.budgetBreakdown = budgetBreakdown;
    }

    public List<String> getTips() {
        return tips;
    }

    public void setTips(List<String> tips) {
        this.tips = tips;
    }
}
