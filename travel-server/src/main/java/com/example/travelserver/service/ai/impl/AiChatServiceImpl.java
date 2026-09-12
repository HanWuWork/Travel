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
 *
 * <p><b>SSE 事件协议</b>（每个事件均有 event 名与 JSON data，字段 type 与 event 名一致）：</p>
 * <ul>
 *   <li>{@code meta}  — 流建立后立即下发：{@code {type, sessionId, provider}}</li>
 *   <li>{@code delta} — 正文增量：{@code {type, content}}</li>
 *   <li>{@code done}  — 回复结束：{@code {type, sessionId, provider, chars}}</li>
 *   <li>{@code error} — 异常：{@code {type, message}}</li>
 * </ul>
 * <p>另有每 15 秒一次的 {@code :ping} 注释行用于保活（客户端应忽略注释行）。</p>
 */
@Service
public class AiChatServiceImpl implements AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);

    /** 每个会话保留的最大历史消息条数，防止内存无限增长 */
    private static final int MAX_HISTORY = 20;

    /** 单条用户消息最大长度 */
    private static final int MAX_MESSAGE_LENGTH = 1000;

    /** SSE 超时时间（毫秒） */
    private static final long SSE_TIMEOUT = 180_000L;

    /** 心跳间隔（毫秒），用于穿过反向代理时保持连接 */
    private static final long HEARTBEAT_INTERVAL = 15_000L;

    /** 会话空闲过期时间（30 分钟），超时后下次访问会重建会话 */
    private static final long SESSION_TTL_MS = 30L * 60 * 1000;

    private final AiAssistant aiAssistant;

    /** 会话存储：sessionId -> 会话（消息历史 + 最近访问时间） */
    private final ConcurrentHashMap<String, SessionEntry> sessions = new ConcurrentHashMap<>();

    /** 会话载体：记录最近访问时间用于过期回收 */
    private static final class SessionEntry {
        final List<ChatMessage> history;
        volatile long lastAccessAt;

        SessionEntry(List<ChatMessage> history) {
            this.history = history;
            this.lastAccessAt = System.currentTimeMillis();
        }
    }

    /** 流式回复专用线程池（守护线程，不阻塞 Web 容器线程） */
    private final ExecutorService streamExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "ai-stream");
        t.setDaemon(true);
        return t;
    });

    /** 心跳调度器 */
    private final java.util.concurrent.ScheduledExecutorService heartbeatScheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "ai-sse-heartbeat");
                t.setDaemon(true);
                return t;
            });

    public AiChatServiceImpl(AiAssistant aiAssistant) {
        this.aiAssistant = aiAssistant;
        // 定期清理已过期会话，避免内存无限增长
        heartbeatScheduler.scheduleAtFixedRate(this::evictExpiredSessions,
                30, 30, java.util.concurrent.TimeUnit.MINUTES);
    }

    /** 清理空闲超过 TTL 的会话 */
    private void evictExpiredSessions() {
        long now = System.currentTimeMillis();
        sessions.entrySet().removeIf(e -> now - e.getValue().lastAccessAt > SESSION_TTL_MS);
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
        // 参数校验在建立 SSE 之前完成，异常将以普通 JSON 响应返回（HTTP 错误），
        // 只有流建立之后的异常才走 error 事件。
        String message = requireMessage(request);
        SessionContext ctx = resolveSession(request);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // 心跳：定期发送注释行，避免代理/网关因长时间无数据而断开
        final java.util.concurrent.ScheduledFuture<?>[] heartbeat = new java.util.concurrent.ScheduledFuture<?>[1];
        Runnable stopHeartbeat = () -> {
            if (heartbeat[0] != null) {
                heartbeat[0].cancel(false);
                heartbeat[0] = null;
            }
        };
        emitter.onCompletion(stopHeartbeat);
        emitter.onTimeout(() -> {
            stopHeartbeat.run();
            emitter.complete();
        });
        emitter.onError(e -> stopHeartbeat.run());

        streamExecutor.execute(() -> {
            StringBuilder fullReply = new StringBuilder();
            try {
                // meta：立即回传会话信息，前端可提前拿到 sessionId
                emitter.send(SseEmitter.event().name("meta")
                        .data(payload("meta", Map.of(
                                "sessionId", ctx.sessionId(),
                                "provider", aiAssistant.provider())),
                                MediaType.APPLICATION_JSON));

                heartbeat[0] = heartbeatScheduler.scheduleAtFixedRate(() -> {
                    try {
                        emitter.send(SseEmitter.event().comment("ping"));
                    } catch (Exception ignored) {
                        // 发送失败说明连接已断，交由后续读写感知
                    }
                }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, java.util.concurrent.TimeUnit.MILLISECONDS);

                aiAssistant.streamReply(message, ctx.history(), chunk -> {
                    fullReply.append(chunk);
                    try {
                        emitter.send(SseEmitter.event().name("delta")
                                .data(payload("delta", Map.of("content", chunk)),
                                        MediaType.APPLICATION_JSON));
                    } catch (IOException e) {
                        // 客户端断开等情况，中断上游读取
                        throw new RuntimeException(e);
                    }
                });

                // 流正常结束：保存会话并推送 done 事件
                String reply = fullReply.toString();
                appendTurn(ctx.history(), message, reply);
                stopHeartbeat.run();
                emitter.send(SseEmitter.event().name("done")
                        .data(payload("done", Map.of(
                                "sessionId", ctx.sessionId(),
                                "provider", aiAssistant.provider(),
                                "chars", reply.length())),
                                MediaType.APPLICATION_JSON));
                emitter.complete();
            } catch (Exception e) {
                stopHeartbeat.run();
                handleStreamError(emitter, e);
            }
        });

        return emitter;
    }

    /** 统一事件体：type 与 event 名保持一致，便于前端按 event 或 type 解析 */
    private Map<String, Object> payload(String type, Map<String, Object> fields) {
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("type", type);
        body.putAll(fields);
        return body;
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
            emitter.send(SseEmitter.event().name("error")
                    .data(payload("error", Map.of("message", msg)), MediaType.APPLICATION_JSON));
            emitter.complete();
        } catch (IOException ioe) {
            emitter.completeWithError(ioe);
        }
    }

    private String requireMessage(ChatRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new BusinessException(400, "消息内容不能为空");
        }
        String message = request.getMessage().trim();
        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new BusinessException(400, "消息过长，请精简后再发送（最多 " + MAX_MESSAGE_LENGTH + " 字）");
        }
        return message;
    }

    /** 解析会话：无 sessionId 则新建；历史优先取服务端会话，回退到前端上送 */
    private SessionContext resolveSession(ChatRequest request) {
        String sessionId = (request.getSessionId() == null || request.getSessionId().isBlank())
                ? newSessionId() : request.getSessionId().trim();

        SessionEntry entry = sessions.get(sessionId);
        long now = System.currentTimeMillis();
        if (entry == null || now - entry.lastAccessAt > SESSION_TTL_MS) {
            if (entry != null) {
                sessions.remove(sessionId);
            }
            entry = new SessionEntry(buildHistoryFromRequest(request));
            sessions.put(sessionId, entry);
        } else {
            entry.lastAccessAt = now;
        }
        return new SessionContext(sessionId, entry.history);
    }

    /** 用前端上送的历史初始化会话：只取最近 MAX_HISTORY 条，并过滤空内容与脏数据 */
    private List<ChatMessage> buildHistoryFromRequest(ChatRequest request) {
        List<ChatMessage> created = new ArrayList<>();
        List<ChatMessage> fromClient = request.getHistory();
        if (fromClient == null || fromClient.isEmpty()) {
            return created;
        }
        int from = Math.max(0, fromClient.size() - MAX_HISTORY);
        for (ChatMessage m : fromClient.subList(from, fromClient.size())) {
            if (m != null && m.getRole() != null && m.getContent() != null && !m.getContent().isBlank()) {
                created.add(m);
            }
        }
        return created;
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
