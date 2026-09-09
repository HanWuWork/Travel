package com.example.travelserver.service.ai.impl;

import com.example.travelserver.common.BusinessException;
import com.example.travelserver.config.TravelAiProperties;
import com.example.travelserver.dto.ai.ChatMessage;
import com.example.travelserver.service.ai.AiAssistant;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 硅基流动（SiliconFlow）大模型实现。
 *
 * <p>通过 OpenAI 兼容协议调用 {@code https://api.siliconflow.cn/v1/chat/completions}，
 * 仅当配置 {@code travel.ai.provider=siliconflow} 时装配生效。支持非流式与 SSE 流式两种调用。</p>
 */
@Component
@ConditionalOnProperty(prefix = "travel.ai", name = "provider", havingValue = "siliconflow")
public class SiliconFlowAssistant implements AiAssistant {

    private static final Logger log = LoggerFactory.getLogger(SiliconFlowAssistant.class);

    private static final String SYSTEM_PROMPT = """
            你是「旅行助手小游」，一个专业、热情的中文旅游顾问。
            职责：为用户推荐旅游目的地、规划行程、介绍景点/美食/最佳出行季节、解答交通住宿与预算问题。
            要求：
            1. 始终使用中文回答，语气亲切自然，回复简洁有条理（可适当使用 emoji）；
            2. 若用户问题与旅游无关，礼貌引导回旅游话题；
            3. 结合对话上下文回答，信息不确定时如实说明，不要编造不存在的景点或价格。
            """;

    private final TravelAiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public SiliconFlowAssistant(TravelAiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        TravelAiProperties.SiliconFlow sf = properties.getSiliconflow();
        this.restClient = RestClient.builder()
                .baseUrl(sf.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + sf.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String reply(String userMessage, List<ChatMessage> history) {
        Map<String, Object> body = buildPayload(userMessage, history, false);

        try {
            SiliconFlowResponse resp = restClient.post()
                    .uri("/chat/completions")
                    .body(body)
                    .retrieve()
                    .body(SiliconFlowResponse.class);

            if (resp == null || resp.choices() == null || resp.choices().isEmpty()
                    || resp.choices().get(0).message() == null) {
                throw new BusinessException(502, "AI 服务返回内容为空，请稍后再试");
            }
            String content = resp.choices().get(0).message().content();
            if (content == null || content.isBlank()) {
                return "抱歉，我暂时没能生成回复，换个问法试试吧～";
            }
            return content.trim();
        } catch (BusinessException e) {
            throw e;
        } catch (RestClientException e) {
            throw mapRemoteException(e);
        }
    }

    @Override
    public void streamReply(String userMessage, List<ChatMessage> history, Consumer<String> onDelta) {
        Map<String, Object> body = buildPayload(userMessage, history, true);

        try {
            restClient.post()
                    .uri("/chat/completions")
                    .body(body)
                    .exchange((request, response) -> {
                        // 先处理非 2xx（401/模型不可用等），读取错误体并转为可读异常
                        if (response.getStatusCode().isError()) {
                            String errorBody = "";
                            try {
                                errorBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
                            } catch (IOException ignored) {
                            }
                            throw mapRemoteException(response.getStatusCode().value(), errorBody);
                        }
                        // 逐行读取上游 SSE：data: {...} / data: [DONE]
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (!line.startsWith("data:")) {
                                    continue;
                                }
                                String data = line.substring(5).trim();
                                if (data.isEmpty() || "[DONE]".equals(data)) {
                                    if ("[DONE]".equals(data)) {
                                        break;
                                    }
                                    continue;
                                }
                                String delta = extractDelta(data);
                                if (delta != null && !delta.isEmpty()) {
                                    onDelta.accept(delta);
                                }
                            }
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                        return null;
                    });
        } catch (BusinessException | UncheckedIOException e) {
            if (e instanceof BusinessException be) {
                throw be;
            }
            throw new BusinessException(502, "读取 AI 流式响应失败，请稍后再试");
        } catch (RestClientException e) {
            throw mapRemoteException(e);
        }
    }

    /** 从上游 SSE 的 data JSON 中提取 choices[0].delta.content */
    private String extractDelta(String dataJson) {
        try {
            JsonNode root = objectMapper.readTree(dataJson);
            JsonNode content = root.path("choices").path(0).path("delta").path("content");
            if (content.isMissingNode() || content.isNull()) {
                return null;
            }
            return content.asText();
        } catch (RuntimeException e) {
            // Jackson 3 异常为非受检；个别非标准分片直接跳过，不中断整个流
            log.debug("跳过无法解析的 SSE 分片: {}", dataJson);
            return null;
        }
    }

    private Map<String, Object> buildPayload(String userMessage, List<ChatMessage> history, boolean stream) {
        TravelAiProperties.SiliconFlow sf = properties.getSiliconflow();
        if (sf.getApiKey() == null || sf.getApiKey().isBlank()) {
            throw new BusinessException(500,
                    "未配置硅基流动 API Key：请设置环境变量 SILICONFLOW_API_KEY，或在 application-local.yml 中填写 travel.ai.siliconflow.api-key");
        }

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
        if (history != null) {
            for (ChatMessage m : history) {
                if (m.getRole() != null && m.getContent() != null && !m.getContent().isBlank()) {
                    messages.add(Map.of("role", m.getRole(), "content", m.getContent()));
                }
            }
        }
        messages.add(Map.of("role", "user", "content", userMessage));

        Map<String, Object> body = new HashMap<>();
        body.put("model", sf.getModel());
        body.put("messages", messages);
        body.put("max_tokens", sf.getMaxTokens());
        body.put("temperature", sf.getTemperature());
        body.put("stream", stream);
        return body;
    }

    private BusinessException mapRemoteException(RestClientException e) {
        log.warn("调用硅基流动失败: {}", e.getMessage());
        return mapRemoteException(0, e.getMessage() == null ? "" : e.getMessage());
    }

    /** 将上游错误（HTTP 状态码 / 错误体）映射为对用户可读的业务异常 */
    private BusinessException mapRemoteException(int status, String detail) {
        log.warn("硅基流动返回错误 status={}, detail={}", status, detail);
        if (status == 401 || detail.contains("401") || detail.contains("Unauthorized")
                || detail.contains("authentication") || detail.contains("api key")) {
            return new BusinessException(500, "硅基流动 API Key 无效或已过期，请检查配置后重试");
        }
        if (detail.contains("model") || detail.contains("Model") || detail.contains("disabled")
                || detail.contains("not found") || detail.contains("不存在")) {
            return new BusinessException(500,
                    "模型不可用或已被禁用（" + properties.getSiliconflow().getModel()
                            + "），请在配置中更换 travel.ai.siliconflow.model");
        }
        if (status == 429 || detail.contains("rate limit") || detail.contains("quota") || detail.contains("余额")) {
            return new BusinessException(500, "AI 服务调用额度不足或触发限流，请稍后再试");
        }
        return new BusinessException(502, "调用硅基流动 AI 服务失败，请稍后再试");
    }

    @Override
    public String provider() {
        return "siliconflow";
    }

    /** 硅基流动 chat/completions 非流式响应（OpenAI 兼容结构） */
    private record SiliconFlowResponse(List<Choice> choices) {
    }

    private record Choice(Message message) {
    }

    private record Message(String role, String content) {
    }
}
