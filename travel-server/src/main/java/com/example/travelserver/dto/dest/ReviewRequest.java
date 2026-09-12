package com.example.travelserver.dto.dest;

/**
 * 评价提交请求
 */
public class ReviewRequest {

    private Long attractionId;
    private Integer rating;
    private String content;
    /** 逗号分隔标签 */
    private String tags;

    public Long getAttractionId() { return attractionId; }
    public void setAttractionId(Long attractionId) { this.attractionId = attractionId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
}
