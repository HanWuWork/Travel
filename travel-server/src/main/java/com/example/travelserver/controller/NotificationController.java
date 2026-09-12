package com.example.travelserver.controller;

import com.example.travelserver.common.UserContext;
import com.example.travelserver.service.user.NotificationService;
import com.example.travelserver.vo.Result;
import com.example.travelserver.vo.user.NotificationVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 消息通知接口（需登录）
 */
@RestController
@RequestMapping("/api/notify")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** 通知列表 */
    @GetMapping("/list")
    public Result<List<NotificationVO>> list(@RequestParam(defaultValue = "false") boolean unreadOnly) {
        return Result.ok(notificationService.list(UserContext.getUserId(), unreadOnly));
    }

    /** 未读数量 */
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount() {
        return Result.ok(Map.of("count", notificationService.unreadCount(UserContext.getUserId())));
    }

    /** 标记单条已读 */
    @PostMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(UserContext.getUserId(), id);
        return Result.ok();
    }

    /** 全部已读 */
    @PostMapping("/read-all")
    public Result<Void> markAllRead() {
        notificationService.markAllRead(UserContext.getUserId());
        return Result.ok();
    }

    /** 删除单条 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        notificationService.delete(UserContext.getUserId(), id);
        return Result.ok();
    }

    /** 清空 */
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        notificationService.clear(UserContext.getUserId());
        return Result.ok();
    }
}
