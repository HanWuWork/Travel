package com.example.travelserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 保存的行程（AI 规划结果持久化）
 */
@Entity
@Table(name = "t_trip")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String destination;

    private Integer budget;

    private Integer days;

    /** 出发日期（yyyy-MM-dd，用于倒计时/日历导出，可空） */
    @Column(name = "start_date", length = 10)
    private String startDate;

    /** 行程结构化 JSON（itinerary/budgetBreakdown/tips/pois 等） */
    @Lob
    @Column(name = "plan_json", length = 65535)
    private String planJson;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    /** 最后更新时间（协作编辑时用于轮询同步） */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /** 版本号：每次协作编辑 +1，前端据此判断是否需要刷新；历史数据可能为 null，按 1 处理 */
    @Column
    private Integer version;

    public Trip() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public Integer getBudget() { return budget; }
    public void setBudget(Integer budget) { this.budget = budget; }
    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getPlanJson() { return planJson; }
    public void setPlanJson(String planJson) { this.planJson = planJson; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
