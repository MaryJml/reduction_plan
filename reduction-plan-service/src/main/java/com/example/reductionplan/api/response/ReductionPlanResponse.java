package com.example.reductionplan.api.response;

import com.example.reductionplan.domain.ReductionPlanStatus;
import com.example.reductionplan.persistence.ReductionPlanEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReductionPlanResponse(
        UUID planId,
        String accountNumber,
        String sortCode,
        BigDecimal reductionAmount,
        ReductionPlanStatus status,
        Instant createdAt
) {

    public static ReductionPlanResponse from(ReductionPlanEntity entity) {
        return new ReductionPlanResponse(
                entity.getPlanId(),
                entity.getAccountNumber(),
                entity.getSortCode(),
                entity.getReductionAmount(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}