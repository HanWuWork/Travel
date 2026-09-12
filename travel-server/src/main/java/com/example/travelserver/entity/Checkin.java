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
 * 城市足迹：用户对城市的打卡状态（去过/想去/计划中）
 */
@Entity
@Table(name = "t_checkin", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "city_id"}))
public class Checkin {

    /** 状态：visited-去过 / wish-想去 / planned-计划中 */
    public static final String VISITED = "visited";
    public static final String WISH = "wish";
    public static final String PLANNED = "planned";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "city_id", nullable = false)
    private Long cityId;

    /** visited / wish / planned */
    @Column(nullable = false, length = 20)
    private String status;

    /** 打卡备注（如"2025年五一"） */
    @Column(length = 200)
    private String note;

    /** 去过次数 */
    @Column(name = "visit_count")
    private Integer visitCount;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    public Checkin() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getCityId() { return cityId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Integer getVisitCount() { return visitCount; }
    public void setVisitCount(Integer visitCount) { this.visitCount = visitCount; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
