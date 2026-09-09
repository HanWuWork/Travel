package com.example.travelserver.vo.ai;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 对话响应数据
 */
@Data
public class ChatReplyVO {

    /** AI 回复内容 */
    private String reply;

    /** 会话ID，前端在后续请求中回传以保持上下文 */
    private String sessionId;

    /** 回复角色，固定为 assistant */
    private String role;

    /** 回复时间 */
    private LocalDateTime timestamp;

    /** AI 提供方标识：mock-内置规则引擎，openai 等-真实大模型 */
    private String provider;
}
