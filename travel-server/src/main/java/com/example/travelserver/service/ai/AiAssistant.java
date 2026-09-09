package com.example.travelserver.service.ai;

import com.example.travelserver.dto.ai.ChatMessage;

import java.util.List;
import java.util.function.Consumer;

/**
 * AI 助手能力抽象。
 *
 * <p>默认提供基于旅游知识库的规则引擎实现（{@code TravelRuleAssistant}），
 * 无需任何外部 API Key 即可运行；后续接入真实大模型时，只需新增一个实现类
 * （例如调用 OpenAI / 通义 / 文心 / 硅基流动等），并通过配置开关切换即可。</p>
 */
public interface AiAssistant {

    /**
     * 根据用户消息与对话上下文生成完整回复（非流式）
     *
     * @param userMessage 用户本次输入
     * @param history     历史对话（按时间顺序，可能为空列表）
     * @return AI 回复文本
     */
    String reply(String userMessage, List<ChatMessage> history);

    /**
     * 流式回复：每生成一段文本就通过 {@code onDelta} 回调推送。
     *
     * <p>默认实现退化为一次性返回完整回复；支持流式的提供方（如硅基流动）应覆写本方法。</p>
     *
     * @param userMessage 用户本次输入
     * @param history     历史对话
     * @param onDelta     正文增量回调
     */
    default void streamReply(String userMessage, List<ChatMessage> history, Consumer<String> onDelta) {
        onDelta.accept(reply(userMessage, history));
    }

    /**
     * 提供方标识，用于在响应中告知前端当前由哪种引擎驱动
     */
    String provider();
}
