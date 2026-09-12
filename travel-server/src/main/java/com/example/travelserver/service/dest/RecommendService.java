package com.example.travelserver.service.dest;

import com.example.travelserver.vo.dest.RecommendVO;

/**
 * 个性化推荐服务：根据用户收藏、行程、足迹生成目的地与景点推荐
 */
public interface RecommendService {

    RecommendVO recommend(Long userId);
}
