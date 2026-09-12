package com.example.travelserver.vo.social;

import lombok.Data;

import java.util.List;

/**
 * 游记 VO
 */
@Data
public class PostVO {

    private Long id;
    private Long userId;
    private String authorName;
    private String authorAvatar;

    private String title;
    private String content;

    /** 正文摘要（列表页使用） */
    private String summary;

    private String city;
    private List<String> tagList;
    private String cover;

    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;

    /** 当前用户是否已点赞 */
    private Boolean liked;

    private String createTime;
}
