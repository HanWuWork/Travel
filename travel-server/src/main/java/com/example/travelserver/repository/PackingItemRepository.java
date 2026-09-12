package com.example.travelserver.repository;

import com.example.travelserver.entity.PackingItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackingItemRepository extends JpaRepository<PackingItem, Long> {

    List<PackingItem> findByUserIdAndTripIdOrderByIdAsc(Long userId, Long tripId);

    List<PackingItem> findByUserIdAndTripIdIsNullOrderByIdAsc(Long userId);

    long countByUserIdAndTripId(Long userId, Long tripId);

    long countByUserIdAndTripIdIsNull(Long userId);
}
