package com.example.travelserver.dto.travel;

import com.example.travelserver.vo.travel.TravelPlanVO;

/**
 * 行程微调请求：基于当前行程 + 一句自然语言要求，让 AI 输出修改后的行程
 */
public class RefineRequest {

    /** 当前行程 */
    private TravelPlanVO plan;

    /** 用户的修改要求，如"第二天太赶了，改轻松点" */
    private String instruction;

    public TravelPlanVO getPlan() { return plan; }
    public void setPlan(TravelPlanVO plan) { this.plan = plan; }
    public String getInstruction() { return instruction; }
    public void setInstruction(String instruction) { this.instruction = instruction; }
}
