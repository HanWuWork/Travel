package com.example.travelserver.vo.dest;

import lombok.Data;

/**
 * 城市足迹 VO：城市信息 + 打卡状态
 */
@Data
public class CheckinVO {

    private Long id;

    private Long cityId;
    private String cityName;
    private String province;
    private Double latitude;
    private Double longitude;

    /** visited / wish / planned */
    private String status;
    private String note;
    private Integer visitCount;
    private String updateTime;
}
