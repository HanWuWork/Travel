package com.example.travelserver.controller;

import com.example.travelserver.service.dest.WeatherService;
import com.example.travelserver.vo.Result;
import com.example.travelserver.vo.dest.WeatherVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 天气接口（公开）：代理 Open-Meteo，支持按城市名或坐标查询
 */
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /** 按城市名查询未来天气 */
    @GetMapping
    public Result<WeatherVO> byCity(@RequestParam String city,
                                    @RequestParam(defaultValue = "7") int days) {
        return Result.ok(weatherService.forecastByCity(city, days));
    }

    /** 按坐标查询未来天气 */
    @GetMapping("/coords")
    public Result<WeatherVO> byCoords(@RequestParam double lat,
                                      @RequestParam double lng,
                                      @RequestParam(defaultValue = "7") int days) {
        return Result.ok(weatherService.forecast(lat, lng, days, null));
    }
}
