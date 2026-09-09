package com.example.travelserver.controller;

import com.example.travelserver.common.UserContext;
import com.example.travelserver.dto.user.LoginRequest;
import com.example.travelserver.dto.user.RegisterRequest;
import com.example.travelserver.entity.User;
import com.example.travelserver.service.user.AuthService;
import com.example.travelserver.vo.Result;
import com.example.travelserver.vo.user.LoginVO;
import com.example.travelserver.vo.user.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：注册、登录、登出、获取当前用户信息
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/register")
    public Result<UserVO> register(@RequestBody RegisterRequest request) {
        return Result.ok(authService.register(request));
    }

    @PostMapping("/auth/login")
    public Result<LoginVO> login(@RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @PostMapping("/auth/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = extractToken(request);
        authService.logout(token);
        return Result.ok();
    }

    /** 获取当前登录用户信息（需登录） */
    @GetMapping("/user/info")
    public Result<UserVO> info() {
        Long userId = UserContext.getUserId();
        User user = authService.findById(userId)
                .orElseThrow(() -> new com.example.travelserver.common.BusinessException(401, "用户不存在"));
        return Result.ok(authService.toVO(user));
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
