package com.example.travelserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 旅行记账：一笔花费
 */
@Entity
@Table(name = "t_expense")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 关联行程 ID（可空，空表示独立记账） */
    @Column(name = "trip_id")
    private Long tripId;

    /** 分类：交通/住宿/餐饮/门票/购物/其他 */
    @Column(nullable = false, length = 20)
    private String category;

    @Column(nullable = false)
    private Double amount;

    @Column(length = 200)
    private String note;

    /** 消费日期 yyyy-MM-dd */
    @Column(name = "expense_date", length = 10)
    private String expenseDate;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    public Expense() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getExpenseDate() { return expenseDate; }
    public void setExpenseDate(String expenseDate) { this.expenseDate = expenseDate; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
