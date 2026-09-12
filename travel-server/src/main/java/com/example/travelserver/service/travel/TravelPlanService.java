package com.example.travelserver.service.travel;

import com.example.travelserver.dto.travel.RefineRequest;
import com.example.travelserver.dto.travel.TravelPlanRequest;
import com.example.travelserver.vo.travel.TravelPlanVO;

/**
 * 旅游规划服务
 */
public interface TravelPlanService {

    /**
     * 根据目的地、预算、天数生成旅游行程规划
     */
    TravelPlanVO plan(TravelPlanRequest request);

    /**
     * 行程微调：基于当前行程与一句自然语言要求，由 AI 输出修改后的完整行程
     */
    TravelPlanVO refine(RefineRequest request);
}
