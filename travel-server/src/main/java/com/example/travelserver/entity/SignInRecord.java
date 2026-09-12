package com.example.travelserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * 每日签到记录（每人每天一条）
 */
@Entity
@Table(name = "t_signin_record", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "sign_date"}))
public class SignInRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 签到日期 yyyy-MM-dd */
    @Column(name = "sign_date", nullable = false, length = 10)
    private String signDate;

    /** 本次获得积分 */
    @Column(nullable = false)
    private Integer points;

    /** 连续签到天数 */
    @Column(name = "consecutive_days", nullable = false)
    private Integer consecutiveDays;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    public SignInRecord() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getSignDate() { return signDate; }
    public void setSignDate(String signDate) { this.signDate = signDate; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public Integer getConsecutiveDays() { return consecutiveDays; }
    public void setConsecutiveDays(Integer consecutiveDays) { this.consecutiveDays = consecutiveDays; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
