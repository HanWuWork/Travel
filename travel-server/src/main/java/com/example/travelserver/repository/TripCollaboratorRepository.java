package com.example.travelserver.repository;

import com.example.travelserver.entity.TripCollaborator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripCollaboratorRepository extends JpaRepository<TripCollaborator, Long> {

    List<TripCollaborator> findByTripIdOrderByJoinedAtAsc(Long tripId);

    List<TripCollaborator> findByUserIdOrderByJoinedAtDesc(Long userId);

    Optional<TripCollaborator> findByTripIdAndUserId(Long tripId, Long userId);

    boolean existsByTripIdAndUserId(Long tripId, Long userId);

    long countByTripId(Long tripId);
}
