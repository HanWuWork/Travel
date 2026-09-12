package com.example.travelserver.vo.travel;

import java.util.List;

/**
 * 单日行程
 */
public class DayPlanVO {

    /** 第几天（从 1 开始） */
    private Integer day;

    /** 当日主题 */
    private String title;

    /** 当日行程描述（概述，兼容旧渲染） */
    private String description;

    /** 温馨提示 */
    private String tip;

    /** 按时间段拆分（上午/中午/下午/晚上） */
    private List<DayPeriodVO> periods;

    public DayPlanVO() {
    }

    public DayPlanVO(Integer day, String title, String description, String tip) {
        this.day = day;
        this.title = title;
        this.description = description;
        this.tip = tip;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public List<DayPeriodVO> getPeriods() {
        return periods;
    }

    public void setPeriods(List<DayPeriodVO> periods) {
        this.periods = periods;
    }
}
