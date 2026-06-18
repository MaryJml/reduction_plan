package com.example.reductionplan.api.response;

import com.example.reductionplan.domain.ReductionPlanStatus;

import java.util.UUID;

public record CreateReductionPlanResponse(
        UUID planId,
        UUID eventId,
        ReductionPlanStatus status,
        String message
) {
}