package com.example.travelserver.controller;

import com.example.travelserver.common.UserContext;
import com.example.travelserver.service.user.PackingService;
import com.example.travelserver.vo.Result;
import com.example.travelserver.vo.user.PackingItemVO;
import com.example.travelserver.vo.user.PackingSummaryVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 打包清单接口（需登录）
 */
@RestController
@RequestMapping("/api/packing")
public class PackingController {

    private final PackingService packingService;

    public PackingController(PackingService packingService) {
        this.packingService = packingService;
    }

    /** 清单（分组 + 完成度） */
    @GetMapping("/list")
    public Result<PackingSummaryVO> list(@RequestParam(required = false) Long tripId) {
        return Result.ok(packingService.summary(UserContext.getUserId(), tripId));
    }

    /** 新增条目 */
    @PostMapping
    public Result<PackingItemVO> add(@RequestParam(required = false) Long tripId,
                                     @RequestParam String name,
                                     @RequestParam(required = false) String category) {
        return Result.ok(packingService.add(UserContext.getUserId(), tripId, name, category));
    }

    /** 勾选/取消勾选 */
    @PostMapping("/{id}/toggle")
    public Result<PackingItemVO> toggle(@PathVariable Long id) {
        return Result.ok(packingService.toggle(UserContext.getUserId(), id));
    }

    /** 一键套用模板 */
    @PostMapping("/template")
    public Result<Map<String, Integer>> applyTemplate(@RequestParam(required = false) Long tripId) {
        int added = packingService.applyTemplate(UserContext.getUserId(), tripId);
        return Result.ok(Map.of("added", added));
    }

    /** 删除条目 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        packingService.delete(UserContext.getUserId(), id);
        return Result.ok();
    }

    /** 清空清单 */
    @DeleteMapping("/clear")
    public Result<Void> clear(@RequestParam(required = false) Long tripId) {
        packingService.clear(UserContext.getUserId(), tripId);
        return Result.ok();
    }
}
