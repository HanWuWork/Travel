package com.example.travelserver.repository;

import com.example.travelserver.entity.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttractionRepository extends JpaRepository<Attraction, Long> {

    List<Attraction> findByCityIdOrderByIdAsc(Long cityId);

    List<Attraction> findByCityIdAndTypeOrderByIdAsc(Long cityId, String type);

    long countByCityId(Long cityId);
}
