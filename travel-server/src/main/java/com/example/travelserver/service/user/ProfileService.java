package com.example.travelserver.service.user;

import com.example.travelserver.dto.user.ProfileUpdateRequest;
import com.example.travelserver.vo.user.UserVO;

/**
 * 个人资料服务
 */
public interface ProfileService {

    /** 获取当前用户资料 */
    UserVO get(Long userId);

    /** 更新资料（昵称/签名/常居地/头像，字段为空则不修改） */
    UserVO update(Long userId, ProfileUpdateRequest request);
}
