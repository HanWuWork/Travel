package com.example.travelserver.controller.ai;

import com.example.travelserver.dto.ai.ChatRequest;
import com.example.travelserver.service.ai.AiChatService;
import com.example.travelserver.vo.Result;
import com.example.travelserver.vo.ai.ChatReplyVO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 旅游助手对话接口
 *
 * <p>接口契约：</p>
 * <ul>
 *   <li>非流式：{@code POST /api/ai/chat}，请求体
 *       {@code { "message": "...", "sessionId": "可选", "history": [] }}，
 *       响应 {@code Result<ChatReplyVO>}</li>
 *   <li>流式：{@code POST /api/ai/chat/stream}，响应 {@code text/event-stream}，
 *       事件 data 为 JSON：{@code {"type":"delta","content":"..."}} /
 *       {@code {"type":"done","sessionId":"...","provider":"..."}} /
 *       {@code {"type":"error","message":"..."}}</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping("/chat")
    public Result<ChatReplyVO> chat(@RequestBody ChatRequest request) {
        return Result.ok(aiChatService.chat(request));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody ChatRequest request) {
        return aiChatService.streamChat(request);
    }
}
