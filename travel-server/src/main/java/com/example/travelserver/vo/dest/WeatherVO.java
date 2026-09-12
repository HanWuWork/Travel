package com.example.travelserver.vo.dest;

import lombok.Data;

import java.util.List;

/**
 * 天气预报 VO
 */
@Data
public class WeatherVO {

    private String city;
    private Double latitude;
    private Double longitude;

    /** 未来数日预报 */
    private List<DayWeatherVO> daily;
}
