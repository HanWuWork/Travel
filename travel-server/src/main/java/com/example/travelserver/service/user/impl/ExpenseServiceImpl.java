package com.example.travelserver.service.user.impl;

import com.example.travelserver.common.BusinessException;
import com.example.travelserver.dto.user.ExpenseRequest;
import com.example.travelserver.entity.Expense;
import com.example.travelserver.entity.Trip;
import com.example.travelserver.repository.ExpenseRepository;
import com.example.travelserver.repository.TripRepository;
import com.example.travelserver.service.user.ExpenseService;
import com.example.travelserver.vo.user.ExpenseStatsVO;
import com.example.travelserver.vo.user.ExpenseVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    /** 分类固定配色，前端图表与列表共用 */
    private static final Map<String, String> CATEGORY_COLORS = Map.of(
            "交通", "#4CCCF4",
            "住宿", "#04DC9C",
            "餐饮", "#2FE0A8",
            "门票", "#BCE4FC",
            "购物", "#E5484D",
            "其他", "#334155"
    );
    private static final List<String> CATEGORIES = List.of("交通", "住宿", "餐饮", "门票", "购物", "其他");

    private final ExpenseRepository expenseRepository;
    private final TripRepository tripRepository;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository, TripRepository tripRepository) {
        this.expenseRepository = expenseRepository;
        this.tripRepository = tripRepository;
    }

    @Override
    @Transactional
    public ExpenseVO add(Long userId, ExpenseRequest request) {
        if (request == null || request.getAmount() == null || request.getAmount() <= 0) {
            throw new BusinessException(400, "请输入有效金额");
        }
        String category = CATEGORIES.contains(request.getCategory()) ? request.getCategory() : "其他";
        Expense e = new Expense();
        e.setUserId(userId);
        e.setTripId(request.getTripId());
        e.setCategory(category);
        e.setAmount(Math.round(request.getAmount() * 100) / 100.0);
        e.setNote(request.getNote());
        e.setExpenseDate(request.getExpenseDate() == null || request.getExpenseDate().isBlank()
                ? LocalDate.now().toString()
                : request.getExpenseDate());
        e.setCreateTime(LocalDateTime.now());
        return toVO(expenseRepository.save(e));
    }

    @Override
    public List<ExpenseVO> list(Long userId, Long tripId) {
        List<Expense> list = tripId == null
                ? expenseRepository.findByUserIdOrderByExpenseDateDescIdDesc(userId)
                : expenseRepository.findByUserIdAndTripIdOrderByExpenseDateDescIdDesc(userId, tripId);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long userId, Long expenseId) {
        Expense e = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new BusinessException(404, "记录不存在"));
        if (!e.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能删除自己的记录");
        }
        expenseRepository.delete(e);
    }

    @Override
    public ExpenseStatsVO stats(Long userId, Long tripId) {
        List<ExpenseVO> list = list(userId, tripId);
        ExpenseStatsVO vo = new ExpenseStatsVO();
        vo.setCount(list.size());

        double rawTotal = list.stream().mapToDouble(ExpenseVO::getAmount).sum();
        double total = Math.round(rawTotal * 100) / 100.0;
        vo.setTotal(total);

        // 分类汇总（按金额降序）
        Map<String, Double> byCat = new LinkedHashMap<>();
        for (ExpenseVO e : list) {
            byCat.merge(e.getCategory(), e.getAmount(), Double::sum);
        }
        List<ExpenseStatsVO.CategoryStat> cats = new ArrayList<>();
        for (Map.Entry<String, Double> entry : byCat.entrySet()) {
            double amount = Math.round(entry.getValue() * 100) / 100.0;
            int percent = total <= 0 ? 0 : (int) Math.round(amount / total * 100);
            cats.add(new ExpenseStatsVO.CategoryStat(
                    entry.getKey(), amount, percent,
                    CATEGORY_COLORS.getOrDefault(entry.getKey(), "#969799")));
        }
        cats.sort(Comparator.comparingDouble(ExpenseStatsVO.CategoryStat::getAmount).reversed());
        vo.setByCategory(cats);

        // 按日期汇总（升序，便于画柱状图）
        Map<String, Double> byDate = new LinkedHashMap<>();
        for (ExpenseVO e : list) {
            byDate.merge(e.getExpenseDate() == null ? "" : e.getExpenseDate(), e.getAmount(), Double::sum);
        }
        List<ExpenseStatsVO.DailyStat> days = byDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(en -> new ExpenseStatsVO.DailyStat(en.getKey(), Math.round(en.getValue() * 100) / 100.0))
                .collect(Collectors.toList());
        vo.setByDate(days);

        // 行程预算对比
        if (tripId != null) {
            tripRepository.findByIdAndUserId(tripId, userId).ifPresent(trip -> {
                vo.setBudget(trip.getBudget());
                if (trip.getBudget() != null) {
                    double remaining = Math.round((trip.getBudget() - total) * 100) / 100.0;
                    vo.setRemaining(remaining);
                    vo.setOverspend(remaining < 0);
                }
            });
        }
        return vo;
    }

    private ExpenseVO toVO(Expense e) {
        ExpenseVO vo = new ExpenseVO();
        vo.setId(e.getId());
        vo.setTripId(e.getTripId());
        vo.setCategory(e.getCategory());
        vo.setAmount(e.getAmount());
        vo.setNote(e.getNote());
        vo.setExpenseDate(e.getExpenseDate());
        return vo;
    }
}
