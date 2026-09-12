package com.example.travelserver.vo.social;

import lombok.Data;

/**
 * 评论 VO
 */
@Data
public class CommentVO {

    private Long id;
    private Long postId;
    private Long userId;
    private String authorName;
    private String authorAvatar;
    private String content;
    private Long replyTo;
    private String replyToName;
    private String createTime;
}
