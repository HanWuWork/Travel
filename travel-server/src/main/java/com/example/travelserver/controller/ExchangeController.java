package com.example.travelserver.controller;

import com.example.travelserver.service.user.ExchangeService;
import com.example.travelserver.vo.Result;
import com.example.travelserver.vo.user.ExchangeVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 汇率换算接口（公开）
 */
@RestController
@RequestMapping("/api/exchange")
public class ExchangeController {

    private final ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @GetMapping
    public Result<ExchangeVO> convert(@RequestParam(defaultValue = "CNY") String from,
                                      @RequestParam(defaultValue = "USD") String to,
                                      @RequestParam(defaultValue = "100") double amount) {
        return Result.ok(exchangeService.convert(from, to, amount));
    }
}
