package com.example.travelserver.vo.dest;

import lombok.Data;

/**
 * 单日天气
 */
@Data
public class DayWeatherVO {

    /** 日期 yyyy-MM-dd */
    private String date;

    /** 星期几（如 周一） */
    private String weekday;

    /** WMO 天气代码 */
    private Integer code;

    /** 天气描述（中文） */
    private String text;

    /** 天气图标（emoji） */
    private String icon;

    private Double tempMax;
    private Double tempMin;

    /** 最大降水概率（%） */
    private Integer precipProb;

    /** 最大风速（km/h） */
    private Double windMax;
}
