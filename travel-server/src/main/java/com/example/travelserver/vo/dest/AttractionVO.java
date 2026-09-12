package com.example.travelserver.vo.dest;

import lombok.Data;

/**
 * 地点（景点/美食/住宿）信息 VO
 */
@Data
public class AttractionVO {

    private Long id;
    private Long cityId;
    private String cityName;
    private String name;

    /** attraction-景点 / food-美食 / hotel-住宿 */
    private String type;
    private String typeLabel;

    private String description;
    private Double latitude;
    private Double longitude;
    private String ticket;
    private String openTime;
    private String playTime;
    private Double rating;

    /** 距搜索中心的距离（公里，仅附近搜索接口返回） */
    private Double distanceKm;
}
