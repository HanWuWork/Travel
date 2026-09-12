package com.example.travelserver.controller;

import com.example.travelserver.common.UserContext;
import com.example.travelserver.service.dest.CheckinService;
import com.example.travelserver.vo.Result;
import com.example.travelserver.vo.dest.CheckinStatsVO;
import com.example.travelserver.vo.dest.CheckinVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 城市足迹接口（需登录）
 */
@RestController
@RequestMapping("/api/checkin")
public class CheckinController {

    private final CheckinService checkinService;

    public CheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    /** 我的足迹列表 */
    @GetMapping("/list")
    public Result<List<CheckinVO>> list() {
        return Result.ok(checkinService.listByUser(UserContext.getUserId()));
    }

    /** 打卡/更新状态 */
    @PostMapping("/mark")
    public Result<CheckinVO> mark(@RequestParam Long cityId,
                                  @RequestParam String status,
                                  @RequestParam(required = false) String note) {
        return Result.ok(checkinService.mark(UserContext.getUserId(), cityId, status, note));
    }

    /** 取消打卡 */
    @DeleteMapping("/remove")
    public Result<Void> remove(@RequestParam Long cityId) {
        checkinService.remove(UserContext.getUserId(), cityId);
        return Result.ok();
    }

    /** 足迹统计 */
    @GetMapping("/stats")
    public Result<CheckinStatsVO> stats() {
        return Result.ok(checkinService.stats(UserContext.getUserId()));
    }
}
