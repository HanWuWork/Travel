package com.example.travelserver.service.ai.impl;

import com.example.travelserver.common.BusinessException;
import com.example.travelserver.dto.ai.ChatMessage;
import com.example.travelserver.dto.ai.ChatRequest;
import com.example.travelserver.service.ai.AiAssistant;
import com.example.travelserver.service.ai.AiChatService;
import com.example.travelserver.vo.ai.ChatReplyVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 对话服务实现：负责会话上下文管理，并委托 {@link AiAssistant} 生成回复。
 *
 * <p>会话历史默认保存在内存中（按 sessionId 隔离），适合演示与单机部署；
 * 后续如需多实例部署，可替换为 Redis 等共享存储。</p>
 */
@Service
public class AiChatServiceImpl implements AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);

    /** 每个会话保留的最大历史消息条数，防止内存无限增长 */
    private static final int MAX_HISTORY = 20;

    /** SSE 超时时间（毫秒） */
    private static final long SSE_TIMEOUT = 180_000L;

    private final AiAssistant aiAssistant;

    /** 会话存储：sessionId -> 消息历史 */
    private final ConcurrentHashMap<String, List<ChatMessage>> sessions = new ConcurrentHashMap<>();

    /** 流式回复专用线程池（守护线程，不阻塞 Web 容器线程） */
    private final ExecutorService streamExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "ai-stream");
        t.setDaemon(true);
        return t;
    });

    public AiChatServiceImpl(AiAssistant aiAssistant) {
        this.aiAssistant = aiAssistant;
    }

    @Override
    public ChatReplyVO chat(ChatRequest request) {
        String message = requireMessage(request);
        SessionContext ctx = resolveSession(request);

        String reply = aiAssistant.reply(message, ctx.history());

        // 记录本轮对话
        appendTurn(ctx.history(), message, reply);

        ChatReplyVO vo = new ChatReplyVO();
        vo.setReply(reply);
        vo.setSessionId(ctx.sessionId());
        vo.setRole("assistant");
        vo.setTimestamp(LocalDateTime.now());
        vo.setProvider(aiAssistant.provider());
        return vo;
    }

    @Override
    public SseEmitter streamChat(ChatRequest request) {
        String message = requireMessage(request);
        SessionContext ctx = resolveSession(request);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitter.onTimeout(emitter::complete);

        streamExecutor.execute(() -> {
            StringBuilder fullReply = new StringBuilder();
            try {
                aiAssistant.streamReply(message, ctx.history(), chunk -> {
                    fullReply.append(chunk);
                    try {
                        emitter.send(SseEmitter.event()
                                .data(Map.of("type", "delta", "content", chunk), MediaType.APPLICATION_JSON));
                    } catch (IOException e) {
                        // 客户端断开等情况，中断上游读取
                        throw new RuntimeException(e);
                    }
                });

                // 流正常结束：保存会话并推送 done 事件
                String reply = fullReply.toString();
                appendTurn(ctx.history(), message, reply);
                emitter.send(SseEmitter.event()
                        .data(Map.of("type", "done",
                                "sessionId", ctx.sessionId(),
                                "provider", aiAssistant.provider()),
                                MediaType.APPLICATION_JSON));
                emitter.complete();
            } catch (Exception e) {
                handleStreamError(emitter, e);
            }
        });

        return emitter;
    }

    /** 流式异常归一化为 error 事件，保证前端能收到可读提示 */
    private void handleStreamError(SseEmitter emitter, Exception e) {
        String msg;
        if (e instanceof BusinessException be) {
            msg = be.getMessage();
        } else if (e.getCause() instanceof IOException) {
            msg = "连接已中断，请重新发送";
        } else {
            log.error("流式对话异常", e);
            msg = "AI 服务暂时不可用，请稍后再试";
        }
        try {
            emitter.send(SseEmitter.event()
                    .data(Map.of("type", "error", "message", msg), MediaType.APPLICATION_JSON));
            emitter.complete();
        } catch (IOException ioe) {
            emitter.completeWithError(ioe);
        }
    }

    private String requireMessage(ChatRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new BusinessException(400, "消息内容不能为空");
        }
        return request.getMessage().trim();
    }

    /** 解析会话：无 sessionId 则新建；历史优先取服务端会话，回退到前端上送 */
    private SessionContext resolveSession(ChatRequest request) {
        String sessionId = (request.getSessionId() == null || request.getSessionId().isBlank())
                ? newSessionId() : request.getSessionId();

        List<ChatMessage> history = sessions.get(sessionId);
        if (history == null) {
            history = new ArrayList<>();
            if (request.getHistory() != null) {
                history.addAll(request.getHistory());
            }
            sessions.put(sessionId, history);
        }
        return new SessionContext(sessionId, history);
    }

    private void appendTurn(List<ChatMessage> history, String userMessage, String reply) {
        if (reply == null || reply.isBlank()) {
            return;
        }
        history.add(ChatMessage.user(userMessage));
        history.add(ChatMessage.assistant(reply.trim()));
        trimHistory(history);
    }

    private String newSessionId() {
        return "sess-" + UUID.randomUUID().toString().replace("-", "");
    }

    private void trimHistory(List<ChatMessage> history) {
        synchronized (history) {
            while (history.size() > MAX_HISTORY) {
                history.remove(0);
            }
        }
    }

    /** 会话上下文内部载体 */
    private record SessionContext(String sessionId, List<ChatMessage> history) {
    }
}
