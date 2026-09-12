package com.example.travelserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 打包清单条目
 */
@Entity
@Table(name = "t_packing_item")
public class PackingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 关联行程（可空） */
    @Column(name = "trip_id")
    private Long tripId;

    @Column(nullable = false, length = 50)
    private String name;

    /** 分类：证件/衣物/电子/洗漱/药品/其他 */
    @Column(nullable = false, length = 20)
    private String category;

    @Column(nullable = false)
    private Boolean packed;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    public PackingItem() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getTripId() { return tripId; }
    public void setTripId(Long tripId) { this.tripId = tripId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Boolean getPacked() { return packed; }
    public void setPacked(Boolean packed) { this.packed = packed; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
