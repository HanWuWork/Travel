package com.example.travelserver.repository;

import com.example.travelserver.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreateTimeDesc(Long userId);

    List<Notification> findByUserIdAndReadOrderByCreateTimeDesc(Long userId, Boolean read);

    long countByUserIdAndRead(Long userId, Boolean read);

    void deleteByUserId(Long userId);
}
