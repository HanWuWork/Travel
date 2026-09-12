package com.example.travelserver.controller;

import com.example.travelserver.common.UserContext;
import com.example.travelserver.service.user.SignInService;
import com.example.travelserver.vo.Result;
import com.example.travelserver.vo.user.SignInStatusVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 每日签到接口（需登录）
 */
@RestController
@RequestMapping("/api/signin")
public class SignInController {

    private final SignInService signInService;

    public SignInController(SignInService signInService) {
        this.signInService = signInService;
    }

    /** 签到状态（含本月日历与积分等级） */
    @GetMapping("/status")
    public Result<SignInStatusVO> status() {
        return Result.ok(signInService.status(UserContext.getUserId()));
    }

    /** 执行签到 */
    @PostMapping
    public Result<SignInStatusVO> signIn() {
        return Result.ok(signInService.signIn(UserContext.getUserId()));
    }
}
