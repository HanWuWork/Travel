package com.example.travelserver.repository;

import com.example.travelserver.entity.TripShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TripShareRepository extends JpaRepository<TripShare, Long> {

    Optional<TripShare> findByTripId(Long tripId);

    Optional<TripShare> findByShareCode(String shareCode);
}
