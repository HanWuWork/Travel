package com.example.travelserver.service.ai;

import com.example.travelserver.dto.ai.ChatRequest;
import com.example.travelserver.vo.ai.ChatReplyVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 对话服务
 */
public interface AiChatService {

    /**
     * 处理一次对话请求，维护多轮会话上下文并返回 AI 回复（非流式）
     */
    ChatReplyVO chat(ChatRequest request);

    /**
     * 流式对话：通过 SSE 逐段推送 AI 回复。
     *
     * <p>事件协议（data 均为 JSON）：</p>
     * <ul>
     *   <li>{@code {"type":"delta","content":"片段"}}}：正文增量，可多次推送</li>
     *   <li>{@code {"type":"done","sessionId":"...","provider":"..."}}：回复结束</li>
     *   <li>{@code {"type":"error","message":"..."}}：发生错误</li>
     * </ul>
     */
    SseEmitter streamChat(ChatRequest request);
}
