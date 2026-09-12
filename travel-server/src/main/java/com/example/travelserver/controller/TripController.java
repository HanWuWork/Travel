package com.example.travelserver.controller;

import com.example.travelserver.common.UserContext;
import com.example.travelserver.dto.travel.TripSaveRequest;
import com.example.travelserver.service.travel.TripService;
import com.example.travelserver.vo.Result;
import com.example.travelserver.vo.travel.TravelPlanVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 行程管理接口（需登录）
 */
@RestController
@RequestMapping("/api/trip")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    /** 保存行程 */
    @PostMapping("/save")
    public Result<TravelPlanVO> save(@RequestBody TripSaveRequest request) {
        return Result.ok(tripService.save(UserContext.getUserId(), request.getStartDate(), request.getPlan()));
    }

    /** 我的行程列表 */
    @GetMapping("/list")
    public Result<List<TravelPlanVO>> list() {
        return Result.ok(tripService.listByUser(UserContext.getUserId()));
    }

    /** 更新行程（微调后保存） */
    @PutMapping("/{id}")
    public Result<TravelPlanVO> update(@PathVariable Long id, @RequestBody TripSaveRequest request) {
        return Result.ok(tripService.update(UserContext.getUserId(), id, request.getStartDate(), request.getPlan()));
    }

    /** 行程详情 */
    @GetMapping("/{id}")
    public Result<TravelPlanVO> get(@PathVariable Long id) {
        return Result.ok(tripService.get(UserContext.getUserId(), id));
    }

    /** 删除行程 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tripService.delete(UserContext.getUserId(), id);
        return Result.ok();
    }
}
