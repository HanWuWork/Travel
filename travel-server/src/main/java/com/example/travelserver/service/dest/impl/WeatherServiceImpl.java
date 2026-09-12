package com.example.travelserver.service.dest.impl;

import com.example.travelserver.common.BusinessException;
import com.example.travelserver.entity.City;
import com.example.travelserver.repository.CityRepository;
import com.example.travelserver.service.dest.WeatherService;
import com.example.travelserver.vo.dest.DayWeatherVO;
import com.example.travelserver.vo.dest.WeatherVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 天气服务实现：调用 Open-Meteo 免费接口（无需 Key），带 30 分钟内存缓存。
 * 接口文档：https://open-meteo.com/en/docs
 */
@Service
public class WeatherServiceImpl implements WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherServiceImpl.class);
    private static final String API = "https://api.open-meteo.com/v1/forecast"
            + "?latitude=%s&longitude=%s"
            + "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,wind_speed_10m_max"
            + "&timezone=Asia%%2FShanghai&forecast_days=%d";
    private static final long CACHE_TTL_MS = 30 * 60 * 1000L;
    private static final String[] WEEKDAYS = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

    private final CityRepository cityRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final Map<String, Cached> cache = new ConcurrentHashMap<>();

    public WeatherServiceImpl(CityRepository cityRepository, ObjectMapper objectMapper) {
        this.cityRepository = cityRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .build();
    }

    @Override
    public WeatherVO forecast(double lat, double lng, int days, String city) {
        int d = Math.max(1, Math.min(days, 16));
        String key = String.format("%.3f,%.3f,%d", lat, lng, d);
        Cached cached = cache.get(key);
        if (cached != null && System.currentTimeMillis() - cached.time < CACHE_TTL_MS) {
            WeatherVO vo = cached.value;
            vo.setCity(city);
            return vo;
        }

        String url = String.format(API, lat, lng, d);
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new BusinessException(503, "天气服务返回异常（" + response.statusCode() + "）");
            }
            WeatherVO vo = parse(response.body(), lat, lng);
            vo.setCity(city);
            cache.put(key, new Cached(vo, System.currentTimeMillis()));
            return vo;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("获取天气失败: {}", e.getMessage());
            throw new BusinessException(503, "天气服务暂时不可用，请稍后再试");
        }
    }

    @Override
    public WeatherVO forecastByCity(String cityName, int days) {
        if (cityName == null || cityName.isBlank()) {
            throw new BusinessException(400, "请提供城市名或坐标");
        }
        String name = cityName.trim().replaceAll("市$", "");
        City city = cityRepository.findAll().stream()
                .filter(c -> c.getName().equals(name) || name.contains(c.getName()) || c.getName().contains(name))
                .findFirst()
                .orElseThrow(() -> new BusinessException(404, "暂未收录该城市的天气，可尝试其他目的地"));
        return forecast(city.getLatitude(), city.getLongitude(), days, city.getName());
    }

    private WeatherVO parse(String body, double lat, double lng) {
        JsonNode root = objectMapper.readTree(body);
        JsonNode daily = root.path("daily");
        List<DayWeatherVO> list = new ArrayList<>();
        JsonNode times = daily.path("time");
        for (int i = 0; i < times.size(); i++) {
            DayWeatherVO day = new DayWeatherVO();
            String date = times.get(i).asText();
            day.setDate(date);
            day.setWeekday(weekday(date));
            int code = daily.path("weather_code").path(i).asInt(0);
            day.setCode(code);
            day.setText(weatherText(code));
            day.setIcon(weatherIcon(code));
            day.setTempMax(round(daily.path("temperature_2m_max").path(i).asDouble()));
            day.setTempMin(round(daily.path("temperature_2m_min").path(i).asDouble()));
            day.setPrecipProb(daily.path("precipitation_probability_max").path(i).asInt(0));
            day.setWindMax(round(daily.path("wind_speed_10m_max").path(i).asDouble()));
            list.add(day);
        }
        WeatherVO vo = new WeatherVO();
        vo.setLatitude(lat);
        vo.setLongitude(lng);
        vo.setDaily(list);
        return vo;
    }

    private String weekday(String date) {
        try {
            return WEEKDAYS[LocalDate.parse(date, DateTimeFormatter.ISO_DATE).getDayOfWeek().getValue() - 1];
        } catch (Exception e) {
            return "";
        }
    }

    private Double round(double v) {
        return Math.round(v * 10) / 10.0;
    }

    /** WMO 天气代码 → 中文描述 */
    private String weatherText(int code) {
        return switch (code) {
            case 0 -> "晴";
            case 1 -> "晴间多云";
            case 2 -> "多云";
            case 3 -> "阴";
            case 45, 48 -> "雾";
            case 51, 53, 55 -> "毛毛雨";
            case 56, 57 -> "冻毛毛雨";
            case 61 -> "小雨";
            case 63 -> "中雨";
            case 65 -> "大雨";
            case 66, 67 -> "冻雨";
            case 71 -> "小雪";
            case 73 -> "中雪";
            case 75 -> "大雪";
            case 77 -> "雪粒";
            case 80, 81 -> "阵雨";
            case 82 -> "强阵雨";
            case 85, 86 -> "阵雪";
            case 95 -> "雷阵雨";
            case 96, 99 -> "雷暴冰雹";
            default -> "未知";
        };
    }

    /** WMO 天气代码 → emoji 图标 */
    private String weatherIcon(int code) {
        return switch (code) {
            case 0 -> "☀️";
            case 1 -> "🌤️";
            case 2 -> "⛅";
            case 3 -> "☁️";
            case 45, 48 -> "🌫️";
            case 51, 53, 55, 56, 57 -> "🌦️";
            case 61, 63, 65, 66, 67 -> "🌧️";
            case 71, 73, 75, 77 -> "❄️";
            case 80, 81, 82 -> "🌦️";
            case 85, 86 -> "🌨️";
            case 95, 96, 99 -> "⛈️";
            default -> "🌡️";
        };
    }

    private record Cached(WeatherVO value, long time) {
    }
}
