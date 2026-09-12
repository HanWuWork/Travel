package com.example.travelserver.repository;

import com.example.travelserver.entity.AttractionReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttractionReviewRepository extends JpaRepository<AttractionReview, Long> {

    List<AttractionReview> findByAttractionIdOrderByUpdateTimeDesc(Long attractionId);

    Optional<AttractionReview> findByAttractionIdAndUserId(Long attractionId, Long userId);

    long countByAttractionId(Long attractionId);

    List<AttractionReview> findByUserIdOrderByUpdateTimeDesc(Long userId);
}
