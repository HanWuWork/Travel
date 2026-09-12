package com.example.travelserver.vo.travel;

import java.util.List;

/**
 * 旅游规划结果
 */
public class TravelPlanVO {

    /** 行程 ID（已保存的行程才有） */
    private Long id;

    /** 出发日期 yyyy-MM-dd（可空） */
    private String startDate;

    /** 目的地 */
    private String destination;

    /** 预算（元） */
    private Integer budget;

    /** 天数 */
    private Integer days;

    /** 每日行程（默认路线，向后兼容地图/导出；等价于 routes[0].itinerary） */
    private List<DayPlanVO> itinerary;

    /** 多条路线方案（经典打卡 / 轻松休闲 / 深度体验） */
    private List<RouteVO> routes;

    /** 预算分配 */
    private List<BudgetItemVO> budgetBreakdown;

    /** 温馨提示 */
    private List<String> tips;

    /** 行程 POI（与库内景点坐标匹配后的结果，用于地图可视化；匹配不到则为空） */
    private List<PlanPoiVO> pois;

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

    public List<RouteVO> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteVO> routes) {
        this.routes = routes;
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

    public List<PlanPoiVO> getPois() {
        return pois;
    }

    public void setPois(List<PlanPoiVO> pois) {
        this.pois = pois;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
}
