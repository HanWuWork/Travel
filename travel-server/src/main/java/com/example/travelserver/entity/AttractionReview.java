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
 * 景点评价：每个用户对同一地点仅一条评价（可修改）
 */
@Entity
@Table(name = "t_attraction_review",
        uniqueConstraints = @UniqueConstraint(columnNames = {"attraction_id", "user_id"}))
public class AttractionReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attraction_id", nullable = false)
    private Long attractionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 评分 1-5 */
    @Column(nullable = false)
    private Integer rating;

    @Column(length = 500)
    private String content;

    /** 标签，逗号分隔 */
    @Column(length = 200)
    private String tags;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    public AttractionReview() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAttractionId() { return attractionId; }
    public void setAttractionId(Long attractionId) { this.attractionId = attractionId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
