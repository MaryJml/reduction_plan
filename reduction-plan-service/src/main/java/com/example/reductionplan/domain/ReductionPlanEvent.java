package com.example.reductionplan.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReductionPlanEvent(
        UUID eventId,   //duplicate event handling
        String eventType,   //easily expend system
        String eventVersion, //support event schema evolution
        UUID planId,
        String accountNumber,
        String sortCode,
        BigDecimal reductionAmount,
        ReductionPlanStatus status,
        Instant occurredAt
) {
}