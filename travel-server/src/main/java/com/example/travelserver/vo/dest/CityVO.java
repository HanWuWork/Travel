package com.example.travelserver.vo.dest;

import lombok.Data;

/**
 * 城市信息 VO
 */
@Data
public class CityVO {

    private Long id;
    private String name;
    private String province;
    private String description;
    private Double latitude;
    private Double longitude;
    private Boolean hot;

    /** 该城市收录的地点数量 */
    private Long spotCount;
}
