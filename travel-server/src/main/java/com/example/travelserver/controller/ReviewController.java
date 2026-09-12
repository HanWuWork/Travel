package com.example.travelserver.controller;

import com.example.travelserver.common.BusinessException;
import com.example.travelserver.common.UserContext;
import com.example.travelserver.dto.dest.ReviewRequest;
import com.example.travelserver.service.dest.ReviewService;
import com.example.travelserver.vo.Result;
import com.example.travelserver.vo.dest.ReviewSummaryVO;
import com.example.travelserver.vo.dest.ReviewVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 景点评价接口：浏览公开，提交/删除需登录
 */
@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /** 某地点评价列表 */
    @GetMapping("/list")
    public Result<List<ReviewVO>> list(@RequestParam Long attractionId) {
        return Result.ok(reviewService.list(UserContext.getUserId(), attractionId));
    }

    /** 某地点评价汇总 */
    @GetMapping("/summary")
    public Result<ReviewSummaryVO> summary(@RequestParam Long attractionId) {
        return Result.ok(reviewService.summary(attractionId));
    }

    /** 我发布的评价 */
    @GetMapping("/mine")
    public Result<List<ReviewVO>> mine() {
        return Result.ok(reviewService.myReviews(requireLogin()));
    }

    /** 提交/更新评价 */
    @PostMapping
    public Result<ReviewVO> submit(@RequestBody ReviewRequest request) {
        return Result.ok(reviewService.submit(requireLogin(), request));
    }

    /** 删除评价 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        reviewService.delete(requireLogin(), id);
        return Result.ok();
    }

    private Long requireLogin() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return userId;
    }
}
