package com.example.travelserver.repository;

import com.example.travelserver.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserIdOrderByExpenseDateDescIdDesc(Long userId);

    List<Expense> findByUserIdAndTripIdOrderByExpenseDateDescIdDesc(Long userId, Long tripId);
}
