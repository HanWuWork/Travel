package com.example.travelserver.repository;

import com.example.travelserver.entity.SignInRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SignInRecordRepository extends JpaRepository<SignInRecord, Long> {

    Optional<SignInRecord> findByUserIdAndSignDate(Long userId, String signDate);

    List<SignInRecord> findByUserIdOrderBySignDateDesc(Long userId);

    List<SignInRecord> findByUserIdAndSignDateBetweenOrderBySignDateAsc(Long userId, String start, String end);

    long countByUserId(Long userId);
}
