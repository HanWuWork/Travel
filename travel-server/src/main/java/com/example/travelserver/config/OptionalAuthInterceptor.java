package com.example.travelserver.config;

import com.example.travelserver.common.UserContext;
import com.example.travelserver.service.user.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 可选鉴权拦截器：有合法 token 时写入 UserContext（便于返回“是否已点赞”等个性化字段），
 * 无 token 或 token 失效时不拦截，游客可正常访问公开内容。用于游记、评价等“可匿名浏览”的接口。
 */
@Component
public class OptionalAuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public OptionalAuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            Long userId = authService.getUserIdByToken(authHeader.substring(7));
            if (userId != null) {
                UserContext.setUserId(userId);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
