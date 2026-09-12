package com.example.travelserver.vo.travel;

/**
 * 行程 POI：AI 行程中提及的地点与库内坐标匹配的结果，用于地图可视化
 */
public class PlanPoiVO {

    /** 匹配到的景点 ID（库内 Attraction），未匹配到具体景点时为空 */
    private Long attractionId;

    /** 展示名称 */
    private String name;

    /** 所属天数（1 开始），0 表示未归属具体某天 */
    private Integer day;

    /** 纬度（WGS-84） */
    private Double latitude;

    /** 经度（WGS-84） */
    private Double longitude;

    /** 顺序号（用于地图连线） */
    private Integer seq;

    public PlanPoiVO() {
    }

    public PlanPoiVO(Long attractionId, String name, Integer day, Double latitude, Double longitude, Integer seq) {
        this.attractionId = attractionId;
        this.name = name;
        this.day = day;
        this.latitude = latitude;
        this.longitude = longitude;
        this.seq = seq;
    }

    public Long getAttractionId() { return attractionId; }
    public void setAttractionId(Long attractionId) { this.attractionId = attractionId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getDay() { return day; }
    public void setDay(Integer day) { this.day = day; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Integer getSeq() { return seq; }
    public void setSeq(Integer seq) { this.seq = seq; }
}
