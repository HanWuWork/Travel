package com.example.travelserver.controller;

import com.example.travelserver.common.BusinessException;
import com.example.travelserver.common.UserContext;
import com.example.travelserver.service.dest.RecommendService;
import com.example.travelserver.vo.Result;
import com.example.travelserver.vo.dest.RecommendVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个性化推荐接口（需登录，基于本人收藏/行程/足迹）
 */
@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    public RecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    @GetMapping
    public Result<RecommendVO> recommend() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return Result.ok(recommendService.recommend(userId));
    }
}
