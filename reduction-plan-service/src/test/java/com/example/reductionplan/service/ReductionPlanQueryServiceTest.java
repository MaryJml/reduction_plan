package com.example.reductionplan.service;

import com.example.reductionplan.api.response.ReductionPlanResponse;
import com.example.reductionplan.domain.ReductionPlanStatus;
import com.example.reductionplan.exception.ReductionPlanNotFoundException;
import com.example.reductionplan.persistence.ReductionPlanEntity;
import com.example.reductionplan.persistence.ReductionPlanRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReductionPlanQueryServiceTest {

    private final ReductionPlanRepository repository = mock(ReductionPlanRepository.class);
    private final ReductionPlanQueryService service = new ReductionPlanQueryService(repository);

    @Test
    void shouldReturnLatestReductionPlanWhenPlanExists() {
        UUID planId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-06-17T10:00:00Z");

        ReductionPlanEntity entity = new ReductionPlanEntity(
                planId,
                "12345678",
                "12-34-56",
                new BigDecimal("500.00"),
                ReductionPlanStatus.ACTIVE,
                createdAt
        );

        when(repository.findTopByAccountNumberAndSortCodeOrderByCreatedAtDesc("12345678", "12-34-56"))
                .thenReturn(Optional.of(entity));

        ReductionPlanResponse response = service.getLatest("12345678", "12-34-56");

        assertThat(response.planId()).isEqualTo(planId);
        assertThat(response.accountNumber()).isEqualTo("12345678");
        assertThat(response.sortCode()).isEqualTo("12-34-56");
        assertThat(response.reductionAmount()).isEqualByComparingTo("500.00");
        assertThat(response.status()).isEqualTo(ReductionPlanStatus.ACTIVE);
        assertThat(response.createdAt()).isEqualTo(createdAt);

        verify(repository).findTopByAccountNumberAndSortCodeOrderByCreatedAtDesc("12345678", "12-34-56");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenPlanDoesNotExist() {
        when(repository.findTopByAccountNumberAndSortCodeOrderByCreatedAtDesc("12345678", "12-34-56"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLatest("12345678", "12-34-56"))
                .isInstanceOf(ReductionPlanNotFoundException.class);

        verify(repository).findTopByAccountNumberAndSortCodeOrderByCreatedAtDesc("12345678", "12-34-56");
    }
}