package com.example.travelserver.vo.user;

import lombok.Data;

import java.util.List;

/**
 * 协作信息 VO
 */
@Data
public class CollabVO {

    private Long tripId;
    private String shareCode;
    private String destination;
    private Integer days;
    private Integer budget;
    private String startDate;

    private Long ownerId;
    private String ownerName;

    /** 当前用户角色：owner / editor / none */
    private String role;

    private long memberCount;

    /** 行程版本号（轮询同步用） */
    private Integer version;

    /** 当前用户的行程数据（加入后返回） */
    private Object plan;

    private List<Member> members;

    @Data
    public static class Member {
        private Long id;
        private Long userId;
        private String name;
        private String avatar;
        private String role;
        private String joinedAt;
    }
}
