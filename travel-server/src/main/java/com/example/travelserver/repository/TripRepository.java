package com.example.travelserver.repository;

import com.example.travelserver.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByUserIdOrderByCreateTimeDesc(Long userId);

    Optional<Trip> findByIdAndUserId(Long id, Long userId);
}
