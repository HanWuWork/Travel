package com.example.travelserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * 景点/美食/住宿等地点信息（坐标为 WGS-84）
 */
@Entity
@Table(name = "t_attraction", indexes = @Index(name = "idx_attraction_city", columnList = "city_id"))
public class Attraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city_id", nullable = false)
    private Long cityId;

    @Column(nullable = false, length = 100)
    private String name;

    /** 类型：attraction-景点 / food-美食 / hotel-住宿 */
    @Column(nullable = false, length = 20)
    private String type;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    /** 门票/人均消费信息 */
    @Column(length = 100)
    private String ticket;

    /** 开放时间 */
    @Column(length = 100)
    private String openTime;

    /** 建议游玩时长 */
    @Column(name = "play_time", length = 50)
    private String playTime;

    /** 评分（0-5） */
    private Double rating;

    public Attraction() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCityId() { return cityId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getTicket() { return ticket; }
    public void setTicket(String ticket) { this.ticket = ticket; }
    public String getOpenTime() { return openTime; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }
    public String getPlayTime() { return playTime; }
    public void setPlayTime(String playTime) { this.playTime = playTime; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
}
