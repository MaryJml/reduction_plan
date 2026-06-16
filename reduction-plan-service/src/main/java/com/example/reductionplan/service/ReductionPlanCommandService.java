package com.example.reductionplan.service;

import com.example.reductionplan.api.request.CreateReductionPlanRequest;
import com.example.reductionplan.api.response.CreateReductionPlanResponse;
import com.example.reductionplan.domain.ReductionPlanEvent;
import com.example.reductionplan.domain.ReductionPlanStatus;
import com.example.reductionplan.messaging.ReductionPlanEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class ReductionPlanCommandService {

    private static final String REDUCTION_PLAN_SUBMITTED_EVENT_TYPE = "REDUCTION_PLAN_SUBMITTED";
    private static final String EVENT_VERSION = "1";

    private final ReductionPlanEventPublisher eventPublisher;
    private final Clock clock;

    public ReductionPlanCommandService(ReductionPlanEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        this.clock = Clock.systemUTC();
    }

    public CreateReductionPlanResponse submit(CreateReductionPlanRequest request) {
        UUID planId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Instant occurredAt = Instant.now(clock);

        ReductionPlanEvent event = new ReductionPlanEvent(
                eventId,
                REDUCTION_PLAN_SUBMITTED_EVENT_TYPE,
                EVENT_VERSION,
                planId,
                request.accountNumber(),
                request.sortCode(),
                request.reductionAmount(),
                ReductionPlanStatus.PENDING,
                occurredAt
        );

        eventPublisher.publish(event);

        return new CreateReductionPlanResponse(
                planId,
                eventId,
                ReductionPlanStatus.PENDING,
                "Reduction plan submitted for processing"
        );
    }
}