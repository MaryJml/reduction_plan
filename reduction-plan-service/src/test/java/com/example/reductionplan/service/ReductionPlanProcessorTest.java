package com.example.reductionplan.service;

import com.example.reductionplan.domain.ReductionPlanEvent;
import com.example.reductionplan.domain.ReductionPlanStatus;
import com.example.reductionplan.persistence.ProcessedEventEntity;
import com.example.reductionplan.persistence.ProcessedEventRepository;
import com.example.reductionplan.persistence.ReductionPlanEntity;
import com.example.reductionplan.persistence.ReductionPlanRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReductionPlanProcessorTest {

    private static final Instant EVENT_OCCURRED_AT = Instant.parse("2026-06-18T10:00:00Z");
    private static final Instant PROCESSED_AT = Instant.parse("2026-06-18T10:00:05Z");

    private final ReductionPlanRepository reductionPlanRepository = mock(ReductionPlanRepository.class);
    private final ProcessedEventRepository processedEventRepository = mock(ProcessedEventRepository.class);
    private final Clock fixedClock = Clock.fixed(PROCESSED_AT, ZoneOffset.UTC);

    private final ReductionPlanProcessor processor = new ReductionPlanProcessor(
            reductionPlanRepository,
            processedEventRepository,
            fixedClock
    );

    @Test
    void shouldPersistReductionPlanAndProcessedEventWhenEventHasNotBeenProcessedBefore() {
        ReductionPlanEvent event = createEvent();

        when(processedEventRepository.existsByEventId(event.eventId()))
                .thenReturn(false);

        processor.process(event);

        ArgumentCaptor<ReductionPlanEntity> reductionPlanCaptor =
                ArgumentCaptor.forClass(ReductionPlanEntity.class);

        verify(reductionPlanRepository).save(reductionPlanCaptor.capture());

        ReductionPlanEntity savedPlan = reductionPlanCaptor.getValue();

        assertThat(savedPlan.getPlanId()).isEqualTo(event.planId());
        assertThat(savedPlan.getAccountNumber()).isEqualTo("12345678");
        assertThat(savedPlan.getSortCode()).isEqualTo("12-34-56");
        assertThat(savedPlan.getReductionAmount()).isEqualByComparingTo("500.00");
        assertThat(savedPlan.getStatus()).isEqualTo(ReductionPlanStatus.ACTIVE);
        assertThat(savedPlan.getCreatedAt()).isEqualTo(EVENT_OCCURRED_AT);

        ArgumentCaptor<ProcessedEventEntity> processedEventCaptor =
                ArgumentCaptor.forClass(ProcessedEventEntity.class);

        verify(processedEventRepository).save(processedEventCaptor.capture());

        ProcessedEventEntity savedProcessedEvent = processedEventCaptor.getValue();

        assertThat(savedProcessedEvent.getEventId()).isEqualTo(event.eventId());
        assertThat(savedProcessedEvent.getProcessedAt()).isEqualTo(PROCESSED_AT);
    }

    @Test
    void shouldIgnoreEventWhenEventHasAlreadyBeenProcessed() {
        ReductionPlanEvent event = createEvent();

        when(processedEventRepository.existsByEventId(event.eventId()))
                .thenReturn(true);

        processor.process(event);

        verify(processedEventRepository).existsByEventId(event.eventId());
        verifyNoInteractions(reductionPlanRepository);
        verify(processedEventRepository, never()).save(any(ProcessedEventEntity.class));
    }

    private ReductionPlanEvent createEvent() {
        return new ReductionPlanEvent(
                UUID.randomUUID(),
                "REDUCTION_PLAN_SUBMITTED",
                "1",
                UUID.randomUUID(),
                "12345678",
                "12-34-56",
                new BigDecimal("500.00"),
                ReductionPlanStatus.PENDING,
                EVENT_OCCURRED_AT
        );
    }
}