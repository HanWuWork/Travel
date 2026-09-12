package com.example.travelserver.repository;

import com.example.travelserver.entity.Checkin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CheckinRepository extends JpaRepository<Checkin, Long> {

    List<Checkin> findByUserIdOrderByUpdateTimeDesc(Long userId);

    Optional<Checkin> findByUserIdAndCityId(Long userId, Long cityId);

    long countByUserIdAndStatus(Long userId, String status);
}
