package com.example.travelserver.dto.ai;

import lombok.Data;

import java.util.List;

/**
 * AI 对话请求
 *
 * 接口契约：POST /api/ai/chat
 */
@Data
public class ChatRequest {

    /** 用户本次输入的消息 */
    private String message;

    /** 会话ID，首次对话可不传，由服务端生成并返回 */
    private String sessionId;

    /** 历史消息（可选），用于多轮对话上下文；服务端也会按 sessionId 维护上下文 */
    private List<ChatMessage> history;
}
