package com.example.travelserver.controller;

import com.example.travelserver.common.UserContext;
import com.example.travelserver.dto.user.ExpenseRequest;
import com.example.travelserver.service.user.ExpenseService;
import com.example.travelserver.vo.Result;
import com.example.travelserver.vo.user.ExpenseStatsVO;
import com.example.travelserver.vo.user.ExpenseVO;
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
 * 旅行记账接口（需登录）
 */
@RestController
@RequestMapping("/api/expense")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    /** 记一笔 */
    @PostMapping
    public Result<ExpenseVO> add(@RequestBody ExpenseRequest request) {
        return Result.ok(expenseService.add(UserContext.getUserId(), request));
    }

    /** 记账列表 */
    @GetMapping("/list")
    public Result<List<ExpenseVO>> list(@RequestParam(required = false) Long tripId) {
        return Result.ok(expenseService.list(UserContext.getUserId(), tripId));
    }

    /** 统计（含分类占比、按日汇总、预算对比） */
    @GetMapping("/stats")
    public Result<ExpenseStatsVO> stats(@RequestParam(required = false) Long tripId) {
        return Result.ok(expenseService.stats(UserContext.getUserId(), tripId));
    }

    /** 删除记录 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        expenseService.delete(UserContext.getUserId(), id);
        return Result.ok();
    }
}
