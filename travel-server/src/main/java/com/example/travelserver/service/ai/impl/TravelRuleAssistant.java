package com.example.travelserver.service.ai.impl;

import com.example.travelserver.dto.ai.ChatMessage;
import com.example.travelserver.service.ai.AiAssistant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 旅游智能助手 —— 基于关键词规则与内置旅游知识库的实现。
 *
 * <p>该实现不依赖任何外部大模型服务，开箱即可运行；能够识别热门旅游城市、
 * 景点推荐、美食、季节天气、交通、住宿、行程预算等常见旅游咨询，
 * 并能结合对话历史中提到的城市进行上下文追问（如先问"成都"，再问"有什么美食"）。</p>
 */
@Component
@ConditionalOnProperty(prefix = "travel.ai", name = "provider", havingValue = "mock", matchIfMissing = true)
public class TravelRuleAssistant implements AiAssistant {

    /** 城市知识库：城市名(含别名) -> 城市介绍 */
    private static final Map<String, String> CITY_GUIDE = new LinkedHashMap<>();
    /** 城市美食 */
    private static final Map<String, String> CITY_FOOD = new LinkedHashMap<>();
    /** 城市最佳旅游季节 */
    private static final Map<String, String> CITY_SEASON = new LinkedHashMap<>();

    static {
        CITY_GUIDE.put("北京", "北京必玩：故宫、八达岭长城、颐和园、天坛、南锣鼓巷。建议安排 3-4 天，感受皇城气派与胡同烟火气。");
        CITY_GUIDE.put("上海", "上海必玩：外滩、迪士尼乐园、豫园、东方明珠、武康路。都市风光与摩登夜景是最大亮点，建议 2-3 天。");
        CITY_GUIDE.put("杭州", "杭州必玩：西湖、灵隐寺、西溪湿地、河坊街。\"上有天堂下有苏杭\"，环湖骑行或乘船都很惬意，建议 2 天。");
        CITY_GUIDE.put("成都", "成都必玩：大熊猫繁育研究基地、宽窄巷子、锦里、都江堰。节奏悠闲，是放松慢游的好地方，建议 2-3 天。");
        CITY_GUIDE.put("西安", "西安必玩：兵马俑、大雁塔、西安城墙、回民街、陕西历史博物馆。十三朝古都，历史迷必去，建议 2-3 天。");
        CITY_GUIDE.put("重庆", "重庆必玩：洪崖洞、解放碑、磁器口古镇、长江索道。8D 魔幻山城，夜景与火锅绝配，建议 2-3 天。");
        CITY_GUIDE.put("厦门", "厦门必玩：鼓浪屿、厦门大学、曾厝垵、环岛路。文艺清新的海滨城市，适合慢游拍照，建议 2-3 天。");
        CITY_GUIDE.put("三亚", "三亚必玩：亚龙湾、蜈支洲岛、天涯海角、南山寺。国内顶级海岛度假地，阳光沙滩一应俱全，建议 3-4 天。");
        CITY_GUIDE.put("云南", "云南必玩：大理古城、洱海、丽江古城、玉龙雪山、香格里拉。自然风光与民族风情兼备，建议 5-7 天深度游。");
        CITY_GUIDE.put("广州", "广州必玩：广州塔、长隆度假区、沙面岛、陈家祠。美食之都，早茶点心不容错过，建议 2-3 天。");

        CITY_FOOD.put("北京", "北京美食：北京烤鸭、炸酱面、涮羊肉、豆汁儿焦圈，前门和簋街都是觅食好去处。");
        CITY_FOOD.put("上海", "上海美食：生煎包、小笼包、红烧肉、本帮菜，南京路和豫园周边小吃很多。");
        CITY_FOOD.put("杭州", "杭州美食：西湖醋鱼、龙井虾仁、东坡肉、叫花鸡，楼外楼是老字号代表。");
        CITY_FOOD.put("成都", "成都美食：火锅、串串香、担担面、龙抄手、钟水饺，麻辣鲜香，锦里和宽窄巷子都能吃个遍。");
        CITY_FOOD.put("西安", "西安美食：肉夹馍、羊肉泡馍、biangbiang面、凉皮，回民街是吃货天堂。");
        CITY_FOOD.put("重庆", "重庆美食：重庆火锅、小面、酸辣粉、毛血旺，麻辣程度比成都更胜一筹。");
        CITY_FOOD.put("厦门", "厦门美食：沙茶面、海蛎煎、姜母鸭、土笋冻，鼓浪屿和曾厝垵有很多特色小店。");
        CITY_FOOD.put("三亚", "三亚美食：各类生猛海鲜、文昌鸡、清补凉、椰子饭，建议去第一市场自选海鲜加工。");
        CITY_FOOD.put("云南", "云南美食：过桥米线、汽锅鸡、鲜花饼、野生菌火锅，大理乳扇和丽江腊排骨也很有特色。");
        CITY_FOOD.put("广州", "广州美食：早茶点心（虾饺、烧卖、肠粉）、煲仔饭、白切鸡，\"食在广州\"名不虚传。");

        CITY_SEASON.put("北京", "北京最佳旅游时间是秋季 9-10 月，天高气爽、香山红叶；春季 4-5 月也不错。");
        CITY_SEASON.put("上海", "上海春秋两季（3-5 月、9-11 月）气候最舒适，适合户外与迪士尼游玩。");
        CITY_SEASON.put("杭州", "杭州春季 3-5 月最佳，西湖烟雨、桃花盛开；秋季 9-10 月满觉陇桂花香。");
        CITY_SEASON.put("成都", "成都 3-6 月和 9-11 月气候宜人，夏季较闷热，冬季阴冷但可吃火锅取暖。");
        CITY_SEASON.put("西安", "西安春秋两季（3-5 月、9-11 月）最佳，夏季炎热、冬季干冷。");
        CITY_SEASON.put("重庆", "重庆春秋最佳，夏季是\"火炉\"非常炎热，冬季湿冷但火锅正当时。");
        CITY_SEASON.put("厦门", "厦门 10-12 月和 3-5 月气候温和，暑期台风较多需留意。");
        CITY_SEASON.put("三亚", "三亚 10 月到次年 4 月是最佳避寒季节，5-9 月炎热但属淡季性价比高。");
        CITY_SEASON.put("云南", "云南四季如春，全年适合旅行，3-5 月看花、9-11 月秋景最佳，注意高原紫外线。");
        CITY_SEASON.put("广州", "广州 10-12 月秋冬最舒适，夏季漫长湿热，注意防暑防雨。");
    }

    @Override
    public String reply(String userMessage, List<ChatMessage> history) {
        String msg = userMessage == null ? "" : userMessage.trim();

        // 1. 城市名直接命中（当前消息优先）
        String city = matchCity(msg);
        // 2. 当前消息无城市时，回溯历史中最近提到的城市，支持上下文追问
        if (city == null) {
            city = recallCityFromHistory(history);
        }

        // 问候
        if (containsAny(msg, "你好", "您好", "hi", "hello", "在吗", "在么", "早上好", "晚上好")) {
            return "你好呀！我是你的专属旅游助手 🧳，可以帮你推荐目的地、规划行程、介绍景点美食，"
                    + "也能回答天气季节、交通住宿等问题。你想去哪里玩呢？";
        }

        // 美食咨询
        if (containsAny(msg, "美食", "吃什么", "好吃", "小吃", "特色菜", "推荐吃", "吃货")) {
            if (city != null) {
                return CITY_FOOD.get(city) + " 需要我再推荐几家具体的店吗？";
            }
            return "说到旅游美食，成都的火锅串串、西安的肉夹馍泡馍、广州的早茶点心、重庆的小面都非常有名！"
                    + "你告诉我想去哪个城市，我可以详细推荐当地特色美食～";
        }

        // 天气 / 最佳季节
        if (containsAny(msg, "天气", "季节", "几月", "什么时候去", "最佳时间", "什么时候", "冷", "热", "穿什么")) {
            if (city != null) {
                return CITY_SEASON.get(city);
            }
            return "不同城市最佳旅游时间不同：想避寒可以冬天去三亚、云南；想看红叶秋天去北京；"
                    + "春秋季大多数城市都比较舒适。你打算去哪个城市？我帮你看看最佳出行时间～";
        }

        // 交通
        if (containsAny(msg, "交通", "怎么去", "高铁", "飞机", "火车", "自驾", "机票", "坐车")) {
            if (city != null) {
                return "去" + city + "一般可以选择飞机或高铁，远途建议飞机省时，周边城市高铁更方便。"
                        + "建议提前 15-30 天订票价格更优，需要我帮你查查大致路线吗？";
            }
            return "出行交通主要看距离：远距离首选飞机，提前订常有折扣；中短途推荐高铁，准时舒适；"
                    + "周边游自驾更自由。你告诉我出发地和目的地，我给你具体建议～";
        }

        // 住宿
        if (containsAny(msg, "住宿", "酒店", "住哪", "住哪里", "民宿", "宾馆", "订房")) {
            if (city != null) {
                return "在" + city + "，想方便逛吃可以住市中心/景区附近，想性价比高可选地铁沿线；"
                        + "度假型目的地（如三亚）建议住海景酒店。建议在正规平台提前预订并看真实评价。";
            }
            return "住宿建议：优先选地铁/公交沿线、靠近主要景区的位置，出行更省时；"
                    + "带老人小孩可选高档酒店，年轻人结伴民宿更有氛围。你打算去哪个城市？";
        }

        // 行程 / 攻略 / 几天
        if (containsAny(msg, "攻略", "行程", "计划", "路线", "几天", "怎么玩", "安排", "旅游")) {
            if (city != null) {
                return CITY_GUIDE.get(city) + " 我可以根据你的天数和偏好帮你排一份详细行程，需要吗？";
            }
            return "规划行程建议先确定目的地和天数，再按\"经典景点 + 美食 + 休闲\"搭配，避免太赶。"
                    + "热门城市如北京、西安、成都、重庆、厦门、三亚都很适合首次出游。你有几天假期、喜欢自然风光还是人文历史？";
        }

        // 预算 / 费用
        if (containsAny(msg, "多少钱", "预算", "费用", "花费", "价格", "便宜", "贵")) {
            if (city != null) {
                return "去" + city + "的花费主要看交通和住宿：一般国内 2-3 天游，不含大交通人均约 1000-2000 元；"
                        + "提前订机票酒店、错峰出行能省不少。需要我帮你估一份详细预算吗？";
            }
            return "旅行预算主要由交通、住宿、门票、餐饮构成。国内周边游人均通常 1000-2000 元/2-3 天，"
                    + "远途或海岛度假会高一些。告诉我目的地和天数，我帮你估算～";
        }

        // 推荐目的地
        if (containsAny(msg, "推荐", "去哪", "哪里好玩", "景点", "好玩的地方", "目的地", "城市")) {
            return "给你推荐几个热门目的地：历史文化选北京、西安；休闲美食选成都、重庆；"
                    + "海滨度假选三亚、厦门；自然风光选云南；都市摩登选上海。你大概有几天假期、喜欢什么类型？我帮你精准推荐～";
        }

        // 感谢 / 道别
        if (containsAny(msg, "谢谢", "感谢", "thanks", "thank you")) {
            return "不客气！祝你旅途愉快 ✈️ 还有其他旅游问题随时问我～";
        }
        if (containsAny(msg, "再见", "拜拜", "bye", "走了", "下次见")) {
            return "再见啦！期待下次继续为你规划旅程，祝你玩得开心 🌏";
        }

        // 命中城市但无明确意图：给出城市攻略
        if (city != null) {
            return CITY_GUIDE.get(city) + " 想了解" + city + "的美食、最佳季节还是行程安排？都可以问我～";
        }

        // 兜底
        return "我是旅游助手，更擅长回答旅游相关的问题哦 😊 比如：\n"
                + "・推荐几个好玩的旅游城市\n"
                + "・成都有什么必玩景点和美食？\n"
                + "・几月份去三亚最合适？\n"
                + "・去西安玩 3 天怎么安排行程？\n"
                + "你想了解哪方面呢？";
    }

    @Override
    public String provider() {
        return "mock";
    }

    /** 在文本中匹配城市名（含常见别名） */
    private String matchCity(String text) {
        if (containsAny(text, "大理", "丽江", "昆明", "洱海", "玉龙雪山", "香格里拉")) {
            return "云南";
        }
        for (String city : CITY_GUIDE.keySet()) {
            if (text.contains(city)) {
                return city;
            }
        }
        return null;
    }

    /** 从历史对话中回溯最近提到的城市 */
    private String recallCityFromHistory(List<ChatMessage> history) {
        if (history == null || history.isEmpty()) {
            return null;
        }
        for (int i = history.size() - 1; i >= 0; i--) {
            ChatMessage m = history.get(i);
            if (m.getContent() == null) {
                continue;
            }
            String city = matchCity(m.getContent());
            if (city != null) {
                return city;
            }
        }
        return null;
    }

    private boolean containsAny(String text, String... keywords) {
        String lower = text.toLowerCase();
        for (String kw : keywords) {
            if (lower.contains(kw.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
