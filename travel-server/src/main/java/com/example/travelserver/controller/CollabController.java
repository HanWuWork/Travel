package com.example.travelserver.controller;

import com.example.travelserver.common.UserContext;
import com.example.travelserver.dto.travel.TripSaveRequest;
import com.example.travelserver.service.user.CollabService;
import com.example.travelserver.vo.Result;
import com.example.travelserver.vo.user.CollabVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 行程协作接口（需登录）
 */
@RestController
@RequestMapping("/api/collab")
public class CollabController {

    private final CollabService collabService;

    public CollabController(CollabService collabService) {
        this.collabService = collabService;
    }

    /** 创建/获取分享码（仅创建者） */
    @PostMapping("/share")
    public Result<CollabVO> share(@RequestParam Long tripId) {
        return Result.ok(collabService.share(UserContext.getUserId(), tripId));
    }

    /** 分享信息（凭分享码） */
    @GetMapping("/info")
    public Result<CollabVO> info(@RequestParam String code) {
        return Result.ok(collabService.info(UserContext.getUserId(), code));
    }

    /** 加入协作 */
    @PostMapping("/join")
    public Result<CollabVO> join(@RequestParam String code) {
        return Result.ok(collabService.join(UserContext.getUserId(), code));
    }

    /** 我参与协作的行程 */
    @GetMapping("/trips")
    public Result<List<CollabVO>> myTrips() {
        return Result.ok(collabService.myCollabTrips(UserContext.getUserId()));
    }

    /** 协作行程详情（含 plan、version，用于轮询同步） */
    @GetMapping("/trips/{tripId}")
    public Result<CollabVO> detail(@PathVariable Long tripId) {
        return Result.ok(collabService.detail(UserContext.getUserId(), tripId));
    }

    /** 协作编辑保存 */
    @PostMapping("/trips/{tripId}")
    public Result<CollabVO> update(@PathVariable Long tripId, @RequestBody TripSaveRequest request) {
        return Result.ok(collabService.update(UserContext.getUserId(), tripId, request.getPlan()));
    }

    /** 成员列表 */
    @GetMapping("/trips/{tripId}/members")
    public Result<List<CollabVO.Member>> members(@PathVariable Long tripId) {
        return Result.ok(collabService.members(UserContext.getUserId(), tripId));
    }

    /** 移除成员 / 退出协作 */
    @DeleteMapping("/trips/{tripId}/members/{memberId}")
    public Result<Void> removeMember(@PathVariable Long tripId, @PathVariable Long memberId) {
        collabService.removeMember(UserContext.getUserId(), tripId, memberId);
        return Result.ok();
    }
}
