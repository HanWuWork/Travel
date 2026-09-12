package com.example.travelserver.service.dest;

import com.example.travelserver.dto.dest.ReviewRequest;
import com.example.travelserver.vo.dest.ReviewSummaryVO;
import com.example.travelserver.vo.dest.ReviewVO;

import java.util.List;

/**
 * 景点评价服务
 */
public interface ReviewService {

    /** 某地点的评价列表 */
    List<ReviewVO> list(Long userId, Long attractionId);

    /** 某地点的评价汇总（平均分、星级分布、热门标签） */
    ReviewSummaryVO summary(Long attractionId);

    /** 提交/更新评价（同一用户对同一地点仅一条） */
    ReviewVO submit(Long userId, ReviewRequest request);

    /** 删除自己的评价 */
    void delete(Long userId, Long reviewId);

    /** 我发布的评价 */
    List<ReviewVO> myReviews(Long userId);
}
