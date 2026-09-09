package com.example.travelserver.service.travel.impl;

import com.example.travelserver.common.BusinessException;
import com.example.travelserver.dto.travel.TravelPlanRequest;
import com.example.travelserver.service.ai.AiAssistant;
import com.example.travelserver.service.travel.TravelPlanService;
import com.example.travelserver.vo.travel.BudgetItemVO;
import com.example.travelserver.vo.travel.DayPlanVO;
import com.example.travelserver.vo.travel.TravelPlanVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 旅游规划服务：调用 AI 生成结构化行程规划（每日行程、预算分配、温馨提示）。
 *
 * <p>通过让 AI 输出严格的 JSON，后端解析后以结构化 VO 返回前端，保证前端可直接渲染。</p>
 */
@Service
public class TravelPlanServiceImpl implements TravelPlanService {

    private static final Logger log = LoggerFactory.getLogger(TravelPlanServiceImpl.class);

    /** 预算类别默认色板，AI 未指定颜色时按顺序使用 */
    private static final String[] DEFAULT_COLORS = {
            "#4A90D9", "#FF6B35", "#52C41A", "#FFB300", "#FF2442",
            "#722ED1", "#13C2C2", "#FA8C16"
    };

    private final AiAssistant aiAssistant;
    private final ObjectMapper objectMapper;

    public TravelPlanServiceImpl(AiAssistant aiAssistant, ObjectMapper objectMapper) {
        this.aiAssistant = aiAssistant;
        this.objectMapper = objectMapper;
    }

    @Override
    public TravelPlanVO plan(TravelPlanRequest request) {
        validate(request);

        String prompt = buildPrompt(request.getDestination(), request.getBudget(), request.getDays());
        String rawReply;
        try {
            rawReply = aiAssistant.reply(prompt, List.of());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用 AI 生成行程失败", e);
            throw new BusinessException(502, "生成行程规划失败，请稍后重试");
        }

        return parsePlan(rawReply, request);
    }

    private void validate(TravelPlanRequest request) {
        if (request == null) {
            throw new BusinessException(400, "请求参数不能为空");
        }
        if (request.getDestination() == null || request.getDestination().isBlank()) {
            throw new BusinessException(400, "请输入旅游目的地");
        }
        if (request.getBudget() == null || request.getBudget() <= 0) {
            throw new BusinessException(400, "请输入有效的预算金额");
        }
        if (request.getDays() == null || request.getDays() <= 0) {
            throw new BusinessException(400, "请输入有效的行程天数");
        }
    }

    /** 构造让 AI 输出严格 JSON 的提示词 */
    private String buildPrompt(String destination, Integer budget, Integer days) {
        return String.format("""
                你是专业的旅游行程规划师。请为用户规划一次【%s】的旅游行程，预算【%d元】，行程【%d天】。
                请严格只输出 JSON（不要任何多余文字、不要 Markdown 代码块标记），结构如下：
                {
                  "itinerary": [
                    {"day": 1, "title": "当日主题", "description": "当日行程安排（具体景点、活动）", "tip": "当日贴心提示"}
                  ],
                  "budgetBreakdown": [
                    {"label": "类别", "percent": 25, "amount": 1250}
                  ],
                  "tips": ["提示1", "提示2"]
                }
                要求：
                1. itinerary 数组长度必须等于 %d 天，day 从 1 递增，最后一天建议安排返程；
                2. 行程要贴合 %s 的真实景点与特色，不要编造不存在的景点；
                3. budgetBreakdown 的 percent 之和等于 100，amount 之和尽量接近 %d；
                4. tips 提供 3-6 条针对本次出行的实用建议；
                5. 所有字段使用中文，description 和 tip 简洁具体。
                """, destination, budget, days, days, destination, budget);
    }

    /** 解析 AI 返回的 JSON；若解析失败则降级生成保底行程，保证页面可用 */
    private TravelPlanVO parsePlan(String rawReply, TravelPlanRequest request) {
        TravelPlanVO vo = new TravelPlanVO();
        vo.setDestination(request.getDestination());
        vo.setBudget(request.getBudget());
        vo.setDays(request.getDays());

        String json = extractJson(rawReply);
        if (json == null) {
            log.warn("AI 未返回有效 JSON，使用保底行程。原始回复: {}", rawReply);
            return fallbackPlan(request);
        }

        try {
            JsonNode root = objectMapper.readTree(json);

            // itinerary
            List<DayPlanVO> itinerary = new ArrayList<>();
            JsonNode itNode = root.path("itinerary");
            if (itNode.isArray()) {
                for (JsonNode item : itNode) {
                    itinerary.add(new DayPlanVO(
                            item.path("day").asInt(itinerary.size() + 1),
                            text(item, "title"),
                            text(item, "description"),
                            text(item, "tip")
                    ));
                }
            }
            // 保底：若 AI 返回的天数不够，补齐
            while (itinerary.size() < request.getDays()) {
                int day = itinerary.size() + 1;
                itinerary.add(new DayPlanVO(day,
                        day == request.getDays() ? "返程" : (request.getDestination() + "自由探索"),
                        day == request.getDays()
                                ? "收拾行李，办理退房，前往机场/车站"
                                : "可根据个人兴趣自由安排活动",
                        "灵活调整行程，享受慢节奏旅行"));
            }
            vo.setItinerary(itinerary);

            // budgetBreakdown
            List<BudgetItemVO> budgetList = new ArrayList<>();
            JsonNode bgNode = root.path("budgetBreakdown");
            if (bgNode.isArray()) {
                int colorIdx = 0;
                for (JsonNode item : bgNode) {
                    String label = text(item, "label");
                    int percent = item.path("percent").asInt(0);
                    int amount = item.path("amount").asInt(
                            Math.round(request.getBudget() * percent / 100f));
                    String color = text(item, "color");
                    if (color == null || color.isBlank()) {
                        color = DEFAULT_COLORS[colorIdx % DEFAULT_COLORS.length];
                    }
                    colorIdx++;
                    budgetList.add(new BudgetItemVO(label, percent, amount, color));
                }
            }
            if (budgetList.isEmpty()) {
                budgetList = defaultBudget(request.getBudget());
            }
            vo.setBudgetBreakdown(budgetList);

            // tips
            List<String> tips = new ArrayList<>();
            JsonNode tipsNode = root.path("tips");
            if (tipsNode.isArray()) {
                for (JsonNode t : tipsNode) {
                    if (t.isTextual() && !t.asText().isBlank()) {
                        tips.add(t.asText());
                    }
                }
            }
            if (tips.isEmpty()) {
                tips = defaultTips();
            }
            vo.setTips(tips);

            return vo;
        } catch (Exception e) {
            log.warn("解析 AI 行程 JSON 失败，使用保底行程: {}", e.getMessage());
            return fallbackPlan(request);
        }
    }

    /** 从 AI 回复中提取 JSON 对象（兼容 AI 可能输出 ```json ... ``` 包裹的情况） */
    private String extractJson(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String text = raw.trim();
        // 去掉 markdown 代码块标记
        text = text.replaceAll("```(?:json)?", "").replaceAll("```", "").trim();
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return null;
    }

    private String text(JsonNode node, String field) {
        JsonNode v = node.path(field);
        if (v.isMissingNode() || v.isNull()) {
            return null;
        }
        String s = v.asText();
        return s.isBlank() ? null : s;
    }

    /** 保底行程：AI 解析失败时使用，保证前端有内容可渲染 */
    private TravelPlanVO fallbackPlan(TravelPlanRequest request) {
        TravelPlanVO vo = new TravelPlanVO();
        vo.setDestination(request.getDestination());
        vo.setBudget(request.getBudget());
        vo.setDays(request.getDays());

        List<DayPlanVO> itinerary = new ArrayList<>();
        String dest = request.getDestination();
        for (int i = 0; i < request.getDays(); i++) {
            int day = i + 1;
            if (day == request.getDays()) {
                itinerary.add(new DayPlanVO(day, "返程",
                        "收拾行李，办理退房，前往机场/车站", "建议提前2小时到达机场"));
            } else {
                itinerary.add(new DayPlanVO(day, dest + "探索日" + day,
                        "游览" + dest + "的经典景点，体验当地文化与美食",
                        "提前购票可享受优惠，注意防晒补水"));
            }
        }
        vo.setItinerary(itinerary);
        vo.setBudgetBreakdown(defaultBudget(request.getBudget()));
        vo.setTips(defaultTips());
        return vo;
    }

    private List<BudgetItemVO> defaultBudget(int total) {
        int[][] items = {
                {25, 0}, {35, 1}, {20, 2}, {12, 3}, {8, 4}
        };
        String[] labels = {"交通", "住宿", "餐饮", "门票", "购物"};
        List<BudgetItemVO> list = new ArrayList<>();
        for (int i = 0; i < items.length; i++) {
            int percent = items[i][0];
            list.add(new BudgetItemVO(labels[i], percent,
                    Math.round(total * percent / 100f), DEFAULT_COLORS[i]));
        }
        return list;
    }

    private List<String> defaultTips() {
        return List.of(
                "出行前请关注目的地天气预报，合理安排行程",
                "建议购买旅游意外险，保障出行安全",
                "提前下载离线地图，方便导航",
                "携带常用药品和充足的饮用水",
                "尊重当地风俗习惯，文明旅游"
        );
    }
}
