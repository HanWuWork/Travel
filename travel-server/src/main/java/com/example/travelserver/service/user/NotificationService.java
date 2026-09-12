package com.example.travelserver.service.user;

import com.example.travelserver.vo.user.NotificationVO;

import java.util.List;

/**
 * 消息通知服务
 */
public interface NotificationService {

    /** 发送通知（内部调用；自己触发给自己的通知会被忽略） */
    void notify(Long userId, Long fromUserId, String type, String title, String content, String link);

    /** 通知列表 */
    List<NotificationVO> list(Long userId, boolean unreadOnly);

    /** 未读数量 */
    long unreadCount(Long userId);

    /** 标记已读 */
    void markRead(Long userId, Long notificationId);

    /** 全部已读 */
    void markAllRead(Long userId);

    /** 删除通知 */
    void delete(Long userId, Long notificationId);

    /** 清空通知 */
    void clear(Long userId);
}
