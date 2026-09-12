package com.example.travelserver.vo.travel;

import java.util.List;

/**
 * 一条完整的路线方案（如 经典打卡 / 轻松休闲 / 深度体验）
 */
public class RouteVO {

    /** 路线序号（从 0 开始） */
    private Integer index;

    /** 路线名称 */
    private String name;

    /** 路线一句话简介 */
    private String summary;

    /** 该路线的每日行程 */
    private List<DayPlanVO> itinerary;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<DayPlanVO> getItinerary() {
        return itinerary;
    }

    public void setItinerary(List<DayPlanVO> itinerary) {
        this.itinerary = itinerary;
    }
}