package com.example.travelserver.dto.social;

/**
 * 游记发布/编辑请求
 */
public class PostRequest {

    private String title;
    private String content;
    private String city;
    /** 逗号分隔标签 */
    private String tags;
    private String cover;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }
}
