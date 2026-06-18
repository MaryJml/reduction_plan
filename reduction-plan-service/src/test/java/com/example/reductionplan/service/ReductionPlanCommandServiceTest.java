package com.example.reductionplan.service;

import com.example.reductionplan.api.request.CreateReductionPlanRequest;
import com.example.reductionplan.api.response.CreateReductionPlanResponse;
import com.example.reductionplan.domain.ReductionPlanEvent;
import com.example.reductionplan.domain.ReductionPlanStatus;
import com.example.reductionplan.messaging.ReductionPlanEventPublisher;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ReductionPlanCommandServiceTest {

    private static final Instant FIXED_TIME = Instant.parse("2026-06-16T10:00:00Z");

    @Test
    void shouldPublishPendingReductionPlanEventAndReturnAcceptedResponse() {
        CapturingReductionPlanEventPublisher eventPublisher = new CapturingReductionPlanEventPublisher();
        Clock fixedClock = Clock.fixed(FIXED_TIME, ZoneOffset.UTC);

        ReductionPlanCommandService service = new ReductionPlanCommandService(
                eventPublisher,
                fixedClock
        );

        CreateReductionPlanRequest request = new CreateReductionPlanRequest(
                "12345678",
                "12-34-56",
                new BigDecimal("500.00")
        );

        CreateReductionPlanResponse response = service.submit(request);

        assertThat(response.planId()).isNotNull();
        assertThat(response.eventId()).isNotNull();
        assertThat(response.status()).isEqualTo(ReductionPlanStatus.PENDING);
        assertThat(response.message()).isEqualTo("Reduction plan submitted for processing");

        ReductionPlanEvent publishedEvent = eventPublisher.publishedEvent;

        assertThat(publishedEvent).isNotNull();
        assertThat(publishedEvent.eventId()).isEqualTo(response.eventId());
        assertThat(publishedEvent.planId()).isEqualTo(response.planId());
        assertThat(publishedEvent.eventType()).isEqualTo("REDUCTION_PLAN_SUBMITTED");
        assertThat(publishedEvent.eventVersion()).isEqualTo("1");
        assertThat(publishedEvent.accountNumber()).isEqualTo("12345678");
        assertThat(publishedEvent.sortCode()).isEqualTo("12-34-56");
        assertThat(publishedEvent.reductionAmount()).isEqualByComparingTo("500.00");
        assertThat(publishedEvent.status()).isEqualTo(ReductionPlanStatus.PENDING);
        assertThat(publishedEvent.occurredAt()).isEqualTo(FIXED_TIME);
    }

    private static class CapturingReductionPlanEventPublisher implements ReductionPlanEventPublisher {

        private ReductionPlanEvent publishedEvent;

        @Override
        public void publish(ReductionPlanEvent event) {
            this.publishedEvent = event;
        }
    }
}