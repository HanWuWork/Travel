package com.example.travelserver.vo.user;

import lombok.Data;

/**
 * 通知 VO
 */
@Data
public class NotificationVO {

    private Long id;
    private String type;
    private String typeLabel;
    private String title;
    private String content;
    private String link;
    private Boolean read;
    private String fromName;
    private String fromAvatar;
    private String createTime;
}
