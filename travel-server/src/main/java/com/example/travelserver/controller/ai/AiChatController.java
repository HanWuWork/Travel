package com.example.travelserver.controller.ai;

import com.example.travelserver.dto.ai.ChatRequest;
import com.example.travelserver.service.ai.AiChatService;
import com.example.travelserver.vo.Result;
import com.example.travelserver.vo.ai.ChatReplyVO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 旅游助手对话接口（统一契约）
 *
 * <p>两个端点共用同一请求体 {@link ChatRequest}：
 * {@code { "message": "必填，≤1000字", "sessionId": "可选，首轮不传", "history": "可选，最多取最近20条" }}</p>
 *
 * <ul>
 *   <li><b>非流式</b>：{@code POST /api/ai/chat} → {@code Result<ChatReplyVO>}
 *       （字段：reply / sessionId / role / timestamp / provider）</li>
 *   <li><b>流式</b>：{@code POST /api/ai/chat/stream} → {@code text/event-stream}，
 *       事件名与 data.type 一致：
 *       <pre>
 * event: meta   data: {"type":"meta","sessionId":"sess-xxx","provider":"siliconflow"}
 * event: delta  data: {"type":"delta","content":"增量片段"}
 * event: done   data: {"type":"done","sessionId":"sess-xxx","provider":"siliconflow","chars":123}
 * event: error  data: {"type":"error","message":"可读错误信息"}
 *       </pre>
 *       另有每 15s 的 {@code :ping} 注释行保活（客户端忽略即可）。</li>
 * </ul>
 *
 * <p>参数校验失败发生在建立 SSE 之前，因此会以普通 JSON（HTTP 400/500）返回；
 * 流建立之后的异常才通过 {@code error} 事件下发。</p>
 */
@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    /** 非流式对话：一次性返回完整回复 */
    @PostMapping("/chat")
    public Result<ChatReplyVO> chat(@RequestBody ChatRequest request) {
        return Result.ok(aiChatService.chat(request));
    }

    /** 流式对话：SSE 增量下发 */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> streamChat(@RequestBody ChatRequest request) {
        SseEmitter emitter = aiChatService.streamChat(request);
        return ResponseEntity.ok()
                // 关闭各级缓冲，保证增量实时到达浏览器
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-transform")
                .header(HttpHeaders.CONNECTION, "keep-alive")
                .header("X-Accel-Buffering", "no")
                .body(emitter);
    }
}
