package com.example.travelserver.vo.user;

import lombok.Data;

/**
 * 打包清单条目 VO
 */
@Data
public class PackingItemVO {

    private Long id;
    private Long tripId;
    private String name;
    private String category;
    private Boolean packed;
}
