package com.example.travelserver.dto.user;

/**
 * 个人资料更新请求（字段为空表示不修改）
 */
public class ProfileUpdateRequest {

    private String nickname;
    private String bio;
    private String city;
    private String avatar;

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}
