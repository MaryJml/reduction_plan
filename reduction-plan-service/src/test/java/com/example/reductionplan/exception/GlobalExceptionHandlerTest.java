package com.example.reductionplan.exception;

import com.example.reductionplan.api.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private static final Instant FIXED_TIME = Instant.parse("2026-06-18T00:30:00Z");

    private final Clock fixedClock = Clock.fixed(FIXED_TIME, ZoneOffset.UTC);
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(fixedClock);

    @Test
    void shouldReturnServiceUnavailableWhenEventPublishingFails() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/reduction-plans");

        EventPublishingException exception = new EventPublishingException(
                "Failed to publish reduction plan event to Kafka",
                new RuntimeException("Kafka unavailable")
        );

        ResponseEntity<ErrorResponse> response = handler.handleEventPublishingException(
                exception,
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().timestamp()).isEqualTo(FIXED_TIME);
        assertThat(response.getBody().status()).isEqualTo(503);
        assertThat(response.getBody().error()).isEqualTo("Service Unavailable");
        assertThat(response.getBody().message())
                .isEqualTo("Unable to publish reduction plan event. Please try again later.");
        assertThat(response.getBody().path()).isEqualTo("/api/reduction-plans");
        assertThat(response.getBody().fieldErrors()).isEmpty();
    }
}