package com.example.travelserver.service.user;

import com.example.travelserver.dto.user.ExpenseRequest;
import com.example.travelserver.vo.user.ExpenseStatsVO;
import com.example.travelserver.vo.user.ExpenseVO;

import java.util.List;

/**
 * 旅行记账服务
 */
public interface ExpenseService {

    /** 记一笔 */
    ExpenseVO add(Long userId, ExpenseRequest request);

    /** 记账列表（tripId 为空时返回全部） */
    List<ExpenseVO> list(Long userId, Long tripId);

    /** 删除 */
    void delete(Long userId, Long expenseId);

    /** 统计（tripId 为空时统计全部；有 tripId 时同时返回预算对比） */
    ExpenseStatsVO stats(Long userId, Long tripId);
}
