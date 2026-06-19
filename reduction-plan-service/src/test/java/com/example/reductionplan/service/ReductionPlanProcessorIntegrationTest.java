package com.example.reductionplan.service;

import com.example.reductionplan.config.ClockConfig;
import com.example.reductionplan.domain.ReductionPlanEvent;
import com.example.reductionplan.domain.ReductionPlanStatus;
import com.example.reductionplan.persistence.ProcessedEventRepository;
import com.example.reductionplan.persistence.ReductionPlanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({ReductionPlanProcessor.class, ClockConfig.class})
class ReductionPlanProcessorIntegrationTest {

    @Autowired
    private ReductionPlanProcessor processor;

    @Autowired
    private ReductionPlanRepository reductionPlanRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Test
    void shouldPersistOnlyOneReductionPlanWhenSameEventIsProcessedTwice() {
        UUID eventId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();

        ReductionPlanEvent event = new ReductionPlanEvent(
                eventId,
                "REDUCTION_PLAN_SUBMITTED",
                "1",
                planId,
                "12345678",
                "12-34-56",
                new BigDecimal("500.00"),
                ReductionPlanStatus.PENDING,
                Instant.parse("2026-06-18T10:00:00Z")
        );

        processor.process(event);
        processor.process(event);

        assertThat(reductionPlanRepository.findAll()).hasSize(1);
        assertThat(processedEventRepository.findAll()).hasSize(1);

        assertThat(reductionPlanRepository.findByPlanId(planId)).isPresent();
        assertThat(processedEventRepository.existsByEventId(eventId)).isTrue();
    }
}