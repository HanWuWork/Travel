package com.example.travelserver.service.user.impl;

import com.example.travelserver.common.BusinessException;
import com.example.travelserver.entity.Notification;
import com.example.travelserver.entity.User;
import com.example.travelserver.repository.NotificationRepository;
import com.example.travelserver.repository.UserRepository;
import com.example.travelserver.service.user.NotificationService;
import com.example.travelserver.vo.user.NotificationVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Map<String, String> TYPE_LABELS = Map.of(
            Notification.LIKE, "点赞",
            Notification.COMMENT, "评论",
            Notification.COLLAB, "协作",
            Notification.SYSTEM, "系统"
    );

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void notify(Long userId, Long fromUserId, String type, String title, String content, String link) {
        if (userId == null || userId.equals(fromUserId)) {
            return;
        }
        Notification n = new Notification();
        n.setUserId(userId);
        n.setFromUserId(fromUserId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setLink(link);
        n.setRead(false);
        n.setCreateTime(LocalDateTime.now());
        notificationRepository.save(n);
    }

    @Override
    public List<NotificationVO> list(Long userId, boolean unreadOnly) {
        List<Notification> list = unreadOnly
                ? notificationRepository.findByUserIdAndReadOrderByCreateTimeDesc(userId, false)
                : notificationRepository.findByUserIdOrderByCreateTimeDesc(userId);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndRead(userId, false);
    }

    @Override
    @Transactional
    public void markRead(Long userId, Long notificationId) {
        Notification n = mustGet(userId, notificationId);
        n.setRead(true);
        notificationRepository.save(n);
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadOrderByCreateTimeDesc(userId, false);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long notificationId) {
        notificationRepository.delete(mustGet(userId, notificationId));
    }

    @Override
    @Transactional
    public void clear(Long userId) {
        notificationRepository.deleteAll(notificationRepository.findByUserIdOrderByCreateTimeDesc(userId));
    }

    private Notification mustGet(Long userId, Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "通知不存在"));
        if (!n.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作该通知");
        }
        return n;
    }

    private NotificationVO toVO(Notification n) {
        NotificationVO vo = new NotificationVO();
        vo.setId(n.getId());
        vo.setType(n.getType());
        vo.setTypeLabel(TYPE_LABELS.getOrDefault(n.getType(), "通知"));
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setLink(n.getLink());
        vo.setRead(n.getRead());
        if (n.getFromUserId() != null) {
            userRepository.findById(n.getFromUserId()).ifPresent((User u) -> {
                vo.setFromName(u.getNickname() == null ? u.getUsername() : u.getNickname());
                vo.setFromAvatar(u.getAvatar());
            });
        }
        vo.setCreateTime(n.getCreateTime() == null ? null : n.getCreateTime().format(FMT));
        return vo;
    }
}
