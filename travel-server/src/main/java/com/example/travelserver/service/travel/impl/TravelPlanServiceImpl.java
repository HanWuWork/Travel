package com.example.travelserver.service.travel.impl;

import com.example.travelserver.common.BusinessException;
import com.example.travelserver.dto.travel.RefineRequest;
import com.example.travelserver.dto.travel.TravelPlanRequest;
import com.example.travelserver.service.ai.AiAssistant;
import com.example.travelserver.service.dest.PoiMatchService;
import com.example.travelserver.service.travel.TravelPlanService;
import com.example.travelserver.vo.travel.BudgetItemVO;
import com.example.travelserver.vo.travel.DayPeriodVO;
import com.example.travelserver.vo.travel.DayPlanVO;
import com.example.travelserver.vo.travel.PlanPoiVO;
import com.example.travelserver.vo.travel.RouteVO;
import com.example.travelserver.vo.travel.TravelPlanVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            "#04DC9C", "#4CCCF4", "#BCE4FC", "#2FE0A8", "#0E7490",
            "#1F6FBF", "#334155", "#4A3F7A"
    };

    private final AiAssistant aiAssistant;
    private final ObjectMapper objectMapper;
    private final PoiMatchService poiMatchService;

    public TravelPlanServiceImpl(AiAssistant aiAssistant, ObjectMapper objectMapper, PoiMatchService poiMatchService) {
        this.aiAssistant = aiAssistant;
        this.objectMapper = objectMapper;
        this.poiMatchService = poiMatchService;
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

        TravelPlanVO vo = parsePlan(rawReply, request);
        List<PlanPoiVO> pois = matchPois(vo);
        if (pois.isEmpty()) {
            // 兜底：行程文本中未识别出可用坐标时，用目的地高分景点补齐，保证地图可用
            pois = poiMatchService.topPois(vo.getDestination(), Math.min(6, Math.max(3, vo.getDays() * 2)));
        }
        vo.setPois(pois);
        return vo;
    }

    @Override
    public TravelPlanVO refine(RefineRequest request) {
        if (request == null || request.getPlan() == null) {
            throw new BusinessException(400, "缺少行程数据");
        }
        if (request.getInstruction() == null || request.getInstruction().isBlank()) {
            throw new BusinessException(400, "请输入修改要求");
        }
        TravelPlanVO current = request.getPlan();
        String currentJson;
        try {
            currentJson = objectMapper.writeValueAsString(current);
        } catch (Exception e) {
            throw new BusinessException(500, "行程数据解析失败");
        }

        String prompt = String.format("""
                你是专业的旅游行程规划师。下面是用户当前的行程 JSON：
                %s

                用户希望这样调整：【%s】

                请在保持整体结构不变的前提下，按用户要求修改行程，并严格只输出修改后的完整 JSON（不要任何多余文字、不要 Markdown 代码块标记），结构如下：
                {
                  "itinerary": [{"day": 1, "title": "当日主题", "description": "当日行程安排", "tip": "当日提示"}],
                  "budgetBreakdown": [{"label": "类别", "percent": 25, "amount": 1250}],
                  "tips": ["提示1"]
                }
                要求：
                1. itinerary 天数保持 %d 天不变；
                2. 只调整用户要求的部分，其余安排尽量保持；
                3. percent 之和等于 100，amount 之和接近 %d；
                4. 所有字段使用中文。
                """, currentJson, request.getInstruction().trim(),
                current.getDays() == null ? 1 : current.getDays(),
                current.getBudget() == null ? 0 : current.getBudget());

        String rawReply;
        try {
            rawReply = aiAssistant.reply(prompt, List.of());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 行程微调失败", e);
            throw new BusinessException(502, "行程调整失败，请稍后重试");
        }

        TravelPlanRequest fallback = new TravelPlanRequest();
        fallback.setDestination(current.getDestination());
        fallback.setBudget(current.getBudget());
        fallback.setDays(current.getDays());
        TravelPlanVO vo = parsePlan(rawReply, fallback);
        // 保留原行程的 ID 与出发日期
        vo.setId(current.getId());
        vo.setStartDate(current.getStartDate());

        List<PlanPoiVO> pois = matchPois(vo);
        if (pois.isEmpty()) {
            pois = poiMatchService.topPois(vo.getDestination(), Math.min(6, Math.max(3,
                    (vo.getDays() == null ? 3 : vo.getDays()) * 2)));
        }
        vo.setPois(pois);
        return vo;
    }

    /**
     * 行程 POI 匹配：先匹配目的地城市，再从每日行程描述中抽取地点名与库内坐标做模糊匹配。
     * 抽取策略：按顿号/逗号/分号切分描述中的短语，取长度 2-12 的短语尝试匹配。
     */
    private List<PlanPoiVO> matchPois(TravelPlanVO vo) {
        try {
            Long cityId = poiMatchService.matchCityId(vo.getDestination());
            List<String> names = new ArrayList<>();
            if (vo.getItinerary() != null) {
                for (DayPlanVO day : vo.getItinerary()) {
                    if (day.getDescription() == null) {
                        continue;
                    }
                    for (String phrase : day.getDescription().split("[、，,;；。\\n]")) {
                        String t = phrase.replaceAll("^(游览|前往|参观|漫步|打卡|逛|去|到达|入住|品尝|体验|返程|抵达|欣赏|登上|骑行|乘船|夜游|游|看)", "").trim();
                        t = t.replaceAll("(等景点|等景点|等地|等|景区|风景区|街区|古镇)$", "").trim();
                        if (t.length() >= 2 && t.length() <= 12) {
                            names.add(t);
                        }
                    }
                }
            }
            if (names.isEmpty()) {
                return List.of();
            }
            return poiMatchService.matchPois(cityId, names);
        } catch (Exception e) {
            log.warn("行程 POI 匹配失败（不影响行程展示）: {}", e.getMessage());
            return List.of();
        }
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
                  "routes": [
                    {
                      "name": "经典打卡",
                      "summary": "路线一句话简介",
                      "itinerary": [
                        {
                          "day": 1,
                          "title": "当日主题",
                          "periods": [
                            {"slot": "上午", "content": "上午安排"},
                            {"slot": "中午", "content": "午餐与午间安排"},
                            {"slot": "下午", "content": "下午安排"},
                            {"slot": "晚上", "content": "晚餐与夜间安排"}
                          ],
                          "tip": "当日贴心提示"
                        }
                      ]
                    }
                  ],
                  "budgetBreakdown": [{"label": "类别", "percent": 25, "amount": 1250}],
                  "tips": ["提示1", "提示2"]
                }
                要求：
                1. 必须提供 3 条不同风格的路线方案，name 分别为「经典打卡」「轻松休闲」「深度体验」，summary 各写一句区别性简介；
                2. 每条路线的 itinerary 数组长度必须等于 %d 天，day 从 1 递增，最后一天建议安排返程；
                3. 每天必须包含上午/中午/下午/晚上四个时间段（periods，slot 固定用 上午/中午/下午/晚上），content 要具体到景点、活动、交通、餐饮，不要笼统描述；
                4. 行程要贴合 %s 的真实景点与特色，不要编造不存在的景点；
                5. budgetBreakdown 的 percent 之和等于 100，amount 之和尽量接近 %d（三条路线共用一份即可）；
                6. tips 提供 3-6 条针对本次出行的实用建议；
                7. 所有字段使用中文，content 简洁具体。
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

            // 多条路线方案
            List<RouteVO> routes = new ArrayList<>();
            JsonNode routesNode = root.path("routes");
            if (routesNode.isArray() && routesNode.size() > 0) {
                int idx = 0;
                for (JsonNode r : routesNode) {
                    RouteVO route = new RouteVO();
                    route.setIndex(idx++);
                    route.setName(text(r, "name"));
                    route.setSummary(text(r, "summary"));
                    route.setItinerary(parseItinerary(r.path("itinerary"), request));
                    routes.add(route);
                }
            } else {
                // 兼容旧/单路线输出：无 routes 字段时按单条 itinerary 处理
                RouteVO route = new RouteVO();
                route.setIndex(0);
                route.setName("推荐路线");
                route.setSummary("");
                route.setItinerary(parseItinerary(root.path("itinerary"), request));
                routes.add(route);
            }
            vo.setRoutes(routes);
            // itinerary 指向第一条，向后兼容地图/导出/详情
            vo.setItinerary(routes.get(0).getItinerary());

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

        String[] routeNames = {"经典打卡", "轻松休闲", "深度体验"};
        String[] routeSummaries = {
                "覆盖必去地标与经典景点",
                "节奏舒缓，注重度假体验",
                "深入本地文化与特色街区"
        };
        List<RouteVO> routes = new ArrayList<>();
        for (int r = 0; r < routeNames.length; r++) {
            RouteVO route = new RouteVO();
            route.setIndex(r);
            route.setName(routeNames[r]);
            route.setSummary(routeSummaries[r]);
            List<DayPlanVO> itinerary = new ArrayList<>();
            for (int i = 0; i < request.getDays(); i++) {
                itinerary.add(fallbackDay(request.getDestination(), i + 1, i + 1 == request.getDays()));
            }
            route.setItinerary(itinerary);
            routes.add(route);
        }
        vo.setRoutes(routes);
        vo.setItinerary(routes.get(0).getItinerary());
        vo.setBudgetBreakdown(defaultBudget(request.getBudget()));
        vo.setTips(defaultTips());
        return vo;
    }

    /** 解析某条路线的每日行程（含四段时间段，天数不足时补齐） */
    private List<DayPlanVO> parseItinerary(JsonNode itNode, TravelPlanRequest request) {
        List<DayPlanVO> itinerary = new ArrayList<>();
        if (itNode != null && itNode.isArray()) {
            for (JsonNode item : itNode) {
                DayPlanVO d = new DayPlanVO();
                d.setDay(item.path("day").asInt(itinerary.size() + 1));
                d.setTitle(text(item, "title"));
                d.setTip(text(item, "tip"));
                d.setPeriods(parsePeriods(item.path("periods")));
                // description 兼容旧渲染：优先取 description，缺失则由四段拼接
                String desc = text(item, "description");
                if (desc == null && d.getPeriods() != null) {
                    desc = d.getPeriods().stream()
                            .map(p -> p.getSlot() + "：" + p.getContent())
                            .collect(Collectors.joining("；"));
                }
                d.setDescription(desc);
                itinerary.add(d);
            }
        }
        while (itinerary.size() < request.getDays()) {
            itinerary.add(fallbackDay(request.getDestination(), itinerary.size() + 1,
                    itinerary.size() + 1 == request.getDays()));
        }
        return itinerary;
    }

    /** 解析单日四段时间段 */
    private List<DayPeriodVO> parsePeriods(JsonNode periodsNode) {
        if (periodsNode == null || !periodsNode.isArray()) {
            return null;
        }
        List<DayPeriodVO> periods = new ArrayList<>();
        for (JsonNode p : periodsNode) {
            String slot = text(p, "slot");
            String content = text(p, "content");
            if (slot != null && content != null) {
                periods.add(new DayPeriodVO(slot, content));
            }
        }
        return periods.isEmpty() ? null : periods;
    }

    /** 保底单日行程（最后一天为返程） */
    private DayPlanVO fallbackDay(String dest, int day, boolean last) {
        if (last) {
            DayPlanVO d = new DayPlanVO(day, "返程", "收拾行李，办理退房，前往机场/车站", "建议提前2小时到达机场");
            d.setPeriods(List.of(
                    new DayPeriodVO("上午", "睡到自然醒，整理行李并办理退房"),
                    new DayPeriodVO("中午", "享用最后一顿当地美食"),
                    new DayPeriodVO("下午", "前往机场/车站，准备返程"),
                    new DayPeriodVO("晚上", "返程途中，注意途中安全")
            ));
            return d;
        }
        DayPlanVO d = new DayPlanVO(day, dest + "探索日" + day,
                "游览" + dest + "的经典景点，体验当地文化与美食", "提前购票可享受优惠，注意防晒补水");
        d.setPeriods(List.of(
                new DayPeriodVO("上午", "前往" + dest + "标志性景点游览"),
                new DayPeriodVO("中午", "品尝当地特色美食"),
                new DayPeriodVO("下午", "参观人文景点或逛特色街区"),
                new DayPeriodVO("晚上", "看夜景、逛夜市，感受城市夜生活")
        ));
        return d;
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
