package com.example.travelserver.service.dest;

import com.example.travelserver.vo.dest.WeatherVO;

/**
 * 天气服务：通过 Open-Meteo 免费接口获取目的地天气预报（无需 API Key）
 */
public interface WeatherService {

    /**
     * 按坐标查询天气
     *
     * @param lat   纬度
     * @param lng   经度
     * @param days  预报天数（1-16）
     * @param city  城市名（可空，仅用于回显）
     */
    WeatherVO forecast(double lat, double lng, int days, String city);

    /** 按城市名查询（从城市库解析坐标） */
    WeatherVO forecastByCity(String cityName, int days);
}
