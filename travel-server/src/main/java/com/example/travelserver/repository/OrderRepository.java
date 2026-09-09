package com.example.travelserver.repository;

import com.example.travelserver.entity.TravelOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<TravelOrder, Long> {
    List<TravelOrder> findByUserIdOrderByCreateTimeDesc(Long userId);
}
