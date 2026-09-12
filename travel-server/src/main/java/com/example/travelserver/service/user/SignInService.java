package com.example.travelserver.service.user;

import com.example.travelserver.vo.user.SignInStatusVO;

/**
 * 每日签到服务
 */
public interface SignInService {

    /** 签到状态（含本月签到日历） */
    SignInStatusVO status(Long userId);

    /** 执行签到，返回最新状态；重复签到抛错 */
    SignInStatusVO signIn(Long userId);
}
