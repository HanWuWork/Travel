package com.example.travelserver.dto.social;

/**
 * 评论请求
 */
public class CommentRequest {

    private Long postId;
    private String content;
    private Long replyTo;

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getReplyTo() { return replyTo; }
    public void setReplyTo(Long replyTo) { this.replyTo = replyTo; }
}
