package com.example.travelserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web 配置：全局跨域 + 登录鉴权拦截器 + 上传文件静态映射
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final OptionalAuthInterceptor optionalAuthInterceptor;

    @Value("${travel.upload.dir:uploads}")
    private String uploadDir;

    @Value("${travel.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}")
    private String allowedOrigins;

    public WebConfig(AuthInterceptor authInterceptor, OptionalAuthInterceptor optionalAuthInterceptor) {
        this.authInterceptor = authInterceptor;
        this.optionalAuthInterceptor = optionalAuthInterceptor;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 上传文件（头像等）：/uploads/** → 本地目录
        String location = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 只允许配置的指定来源（默认开发地址），禁止 "*" + 凭据的组合，避免任意来源携带 Cookie 调用
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 必须登录：用户信息、订单、收藏、行程及个人数据类接口
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(
                        "/api/user/info",
                        "/api/order/**",
                        "/api/favorite/**",
                        "/api/trip/**",
                        "/api/checkin/**",
                        "/api/expense/**",
                        "/api/packing/**",
                        "/api/profile/**",
                        "/api/signin/**",
                        "/api/notify/**",
                        "/api/recommend/**",
                        "/api/collab/**"
                );

        // 可选登录：游记、评价等公开可浏览内容（有 token 则识别用户，无 token 也放行）
        registry.addInterceptor(optionalAuthInterceptor)
                .addPathPatterns(
                        "/api/social/**",
                        "/api/review/**"
                );
    }
}
