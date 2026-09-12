package com.example.travelserver.service.user.impl;

import com.example.travelserver.common.BusinessException;
import com.example.travelserver.service.user.ExchangeService;
import com.example.travelserver.vo.user.ExchangeVO;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 汇率换算实现：调用 open.er-api.com（免费、无需 Key），按源货币缓存 1 小时。
 */
@Service
public class ExchangeServiceImpl implements ExchangeService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeServiceImpl.class);
    private static final String API = "https://open.er-api.com/v6/latest/";
    private static final long CACHE_TTL_MS = 60 * 60 * 1000L;

    /** 常用货币中文名 */
    private static final Map<String, String> CURRENCY_NAMES = new LinkedHashMap<>();

    static {
        CURRENCY_NAMES.put("CNY", "人民币");
        CURRENCY_NAMES.put("USD", "美元");
        CURRENCY_NAMES.put("EUR", "欧元");
        CURRENCY_NAMES.put("JPY", "日元");
        CURRENCY_NAMES.put("KRW", "韩元");
        CURRENCY_NAMES.put("HKD", "港币");
        CURRENCY_NAMES.put("TWD", "新台币");
        CURRENCY_NAMES.put("THB", "泰铢");
        CURRENCY_NAMES.put("SGD", "新加坡元");
        CURRENCY_NAMES.put("MYR", "马来西亚林吉特");
        CURRENCY_NAMES.put("GBP", "英镑");
        CURRENCY_NAMES.put("AUD", "澳元");
        CURRENCY_NAMES.put("CAD", "加元");
        CURRENCY_NAMES.put("CHF", "瑞士法郎");
        CURRENCY_NAMES.put("RUB", "卢布");
        CURRENCY_NAMES.put("INR", "印度卢比");
        CURRENCY_NAMES.put("VND", "越南盾");
        CURRENCY_NAMES.put("PHP", "菲律宾比索");
        CURRENCY_NAMES.put("IDR", "印尼盾");
        CURRENCY_NAMES.put("AED", "迪拉姆");
        CURRENCY_NAMES.put("NZD", "新西兰元");
    }

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final Map<String, CachedRates> cache = new ConcurrentHashMap<>();

    public ExchangeServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .build();
    }

    @Override
    public ExchangeVO convert(String from, String to, double amount) {
        String f = from == null || from.isBlank() ? "CNY" : from.trim().toUpperCase();
        String t = to == null || to.isBlank() ? "USD" : to.trim().toUpperCase();

        Map<String, Double> rates = ratesOf(f);
        Double rate = rates.get(t);
        if (rate == null) {
            throw new BusinessException(400, "暂不支持该货币：" + t);
        }
        double result = Math.round(amount * rate * 100) / 100.0;
        ExchangeVO vo = new ExchangeVO();
        vo.setFrom(f);
        vo.setTo(t);
        vo.setAmount(amount);
        vo.setRate(rate);
        vo.setResult(result);
        vo.setUpdateTime(cache.get(f).updateTime);

        // 人民币源货币时附带常用货币参考汇率
        if ("CNY".equals(f)) {
            List<ExchangeVO.RateItem> common = new ArrayList<>();
            CURRENCY_NAMES.forEach((code, name) -> {
                if (!"CNY".equals(code) && rates.containsKey(code)) {
                    common.add(new ExchangeVO.RateItem(code, name,
                            Math.round(rates.get(code) * 10000) / 10000.0));
                }
            });
            vo.setCommonRates(common);
        }
        return vo;
    }

    private Map<String, Double> ratesOf(String base) {
        CachedRates cached = cache.get(base);
        if (cached != null && System.currentTimeMillis() - cached.fetchTime < CACHE_TTL_MS) {
            return cached.rates;
        }
        String url = API + base;
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(12))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new BusinessException(503, "汇率服务返回异常（" + response.statusCode() + "）");
            }
            JsonNode root = objectMapper.readTree(response.body());
            if (!"success".equals(root.path("result").asText())) {
                throw new BusinessException(503, "汇率服务返回失败结果");
            }
            JsonNode ratesNode = root.path("rates");
            Map<String, Double> rates = new LinkedHashMap<>();
            ratesNode.propertyNames().forEach(name -> rates.put(name, ratesNode.path(name).asDouble()));
            String updateTime = root.path("time_last_update_utc").asText("");
            cache.put(base, new CachedRates(rates, System.currentTimeMillis(), updateTime));
            return rates;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("获取汇率失败: {}", e.getMessage());
            // 有旧缓存则降级使用
            if (cached != null) {
                return cached.rates;
            }
            throw new BusinessException(503, "汇率服务暂时不可用，请稍后再试");
        }
    }

    private record CachedRates(Map<String, Double> rates, long fetchTime, String updateTime) {
    }
}
