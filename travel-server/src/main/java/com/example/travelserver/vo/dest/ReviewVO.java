package com.example.travelserver.vo.dest;

import lombok.Data;

import java.util.List;

/**
 * 评价 VO
 */
@Data
public class ReviewVO {

    private Long id;
    private Long attractionId;
    private Long userId;
    private String authorName;
    private String authorAvatar;
    private Integer rating;
    private String content;
    private List<String> tagList;
    private String createTime;

    /** 是否为当前登录用户所写 */
    private Boolean mine;
}
