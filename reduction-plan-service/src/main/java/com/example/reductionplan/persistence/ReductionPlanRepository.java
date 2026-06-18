package com.example.reductionplan.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReductionPlanRepository extends JpaRepository<ReductionPlanEntity, Long> {

    Optional<ReductionPlanEntity> findByPlanId(UUID planId);

    Optional<ReductionPlanEntity> findTopByAccountNumberAndSortCodeOrderByCreatedAtDesc(
            String accountNumber,
            String sortCode
    );
}