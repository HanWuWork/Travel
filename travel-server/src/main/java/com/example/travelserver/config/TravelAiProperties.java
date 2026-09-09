package com.example.travelserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 对话相关配置（前缀 travel.ai）
 */
@Data
@Component
@ConfigurationProperties(prefix = "travel.ai")
public class TravelAiProperties {

    /** 提供方：mock-内置规则引擎；siliconflow-硅基流动大模型 */
    private String provider = "mock";

    private SiliconFlow siliconflow = new SiliconFlow();

    @Data
    public static class SiliconFlow {
        /** 接口基础地址（OpenAI 兼容协议） */
        private String baseUrl = "https://api.siliconflow.cn/v1";
        /** API Key，建议通过环境变量 SILICONFLOW_API_KEY 注入，勿明文提交到 Git */
        private String apiKey = "";
        /** 模型 id，例如 Qwen/Qwen2.5-7B-Instruct */
        private String model = "Qwen/Qwen2.5-7B-Instruct";
        /** 最大生成 token 数 */
        private int maxTokens = 1024;
        /** 采样温度 0-2 */
        private double temperature = 0.7;
    }
}
