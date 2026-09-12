package com.example.travelserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 行程分享：一个行程对应一个分享码，凭码可加入协作
 */
@Entity
@Table(name = "t_trip_share")
public class TripShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_id", nullable = false, unique = true)
    private Long tripId;

    /** 分享码（8 位大写字母数字） */
    @Column(name = "share_code", nullable = false, unique = true, length = 16)
    private String shareCode;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    /** 是否允许协作者编辑 */
    @Column(name = "allow_edit", nullable = false)
    private Boolean allowEdit;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    public TripShare() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }
    public String getShareCode() { return shareCode; }
    public void setShareCode(String shareCode) { this.shareCode = shareCode; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public Boolean getAllowEdit() { return allowEdit; }
    public void setAllowEdit(Boolean allowEdit) { this.allowEdit = allowEdit; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
