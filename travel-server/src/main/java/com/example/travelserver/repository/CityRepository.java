package com.example.travelserver.repository;

import com.example.travelserver.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {

    List<City> findAllByOrderByIdAsc();

    Optional<City> findByName(String name);
}
