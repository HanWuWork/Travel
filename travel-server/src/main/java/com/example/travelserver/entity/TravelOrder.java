package com.example.travelserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 旅游订单（由行程规划生成并保存）
 */
@Entity
@Table(name = "t_order")
public class TravelOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String destination;

    @Column(nullable = false)
    private Integer days;

    @Column(nullable = false)
    private Integer budget;

    /** 行程 JSON 快照（保存规划结果，便于回看） */
    @Column(name = "plan_json", columnDefinition = "TEXT")
    private String planJson;

    /** 状态：pending-待出行 / completed-已完成 / cancelled-已取消 */
    @Column(length = 20)
    private String status;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    public TravelOrder() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }
    public Integer getBudget() { return budget; }
    public void setBudget(Integer budget) { this.budget = budget; }
    public String getPlanJson() { return planJson; }
    public void setPlanJson(String planJson) { this.planJson = planJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
