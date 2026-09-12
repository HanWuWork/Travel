package com.example.travelserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 消息通知
 */
@Entity
@Table(name = "t_notification", indexes = @Index(name = "idx_notify_user", columnList = "user_id"))
public class Notification {

    /** 类型：like-点赞 / comment-评论 / collab-协作 / system-系统 */
    public static final String LIKE = "like";
    public static final String COMMENT = "comment";
    public static final String COLLAB = "collab";
    public static final String SYSTEM = "system";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 触发者 ID（系统消息为 null） */
    @Column(name = "from_user_id")
    private Long fromUserId;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 300)
    private String content;

    /** 前端跳转路径，如 /posts/1 */
    @Column(length = 200)
    private String link;

    @Column(nullable = false)
    private Boolean read;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    public Notification() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getFromUserId() { return fromUserId; }
    public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
