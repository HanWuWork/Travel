package com.example.travelserver.vo.travel;

/**
 * 单日内的一个时间段安排（上午 / 中午 / 下午 / 晚上）
 */
public class DayPeriodVO {

    /** 时间段名：上午 / 中午 / 下午 / 晚上 */
    private String slot;

    /** 该时间段的具体行程安排 */
    private String content;

    public DayPeriodVO() {
    }

    public DayPeriodVO(String slot, String content) {
        this.slot = slot;
        this.content = content;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}