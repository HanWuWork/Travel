package com.example.travelserver.controller;

import com.example.travelserver.dto.travel.TravelPlanRequest;
import com.example.travelserver.service.travel.TravelPlanService;
import com.example.travelserver.vo.Result;
import com.example.travelserver.vo.travel.TravelPlanVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/travel")
@RestController
public class TravelController {

    private final TravelPlanService travelPlanService;

    public TravelController(TravelPlanService travelPlanService) {
        this.travelPlanService = travelPlanService;
    }

    @GetMapping("/hello")
    public String hello() {
        return "hello world";
    }

    /**
     * 生成旅游行程规划
     * 请求体：{ "destination": "成都", "budget": 3000, "days": 3 }
     */
    @PostMapping("/plan")
    public Result<TravelPlanVO> plan(@RequestBody TravelPlanRequest request) {
        return Result.ok(travelPlanService.plan(request));
    }
}
