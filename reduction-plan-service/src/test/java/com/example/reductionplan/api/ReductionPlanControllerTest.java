package com.example.reductionplan.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.reductionplan.domain.ReductionPlanStatus;
import com.example.reductionplan.persistence.ReductionPlanEntity;
import com.example.reductionplan.persistence.ReductionPlanRepository;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
class ReductionPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReductionPlanRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldReturnAcceptedWhenSubmittingValidReductionPlan() throws Exception {
        String requestBody = """
                {
                  "accountNumber": "12345678",
                  "sortCode": "12-34-56",
                  "reductionAmount": 500.00
                }
                """;

        mockMvc.perform(post("/api/reduction-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.planId", notNullValue()))
                .andExpect(jsonPath("$.eventId", notNullValue()))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.message", is("Reduction plan submitted for processing")));
    }

    @Test
    void shouldReturnBadRequestWhenReductionAmountIsNegative() throws Exception {
        String requestBody = """
                {
                  "accountNumber": "12345678",
                  "sortCode": "12-34-56",
                  "reductionAmount": -100.00
                }
                """;

        mockMvc.perform(post("/api/reduction-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenReductionAmountIsZero() throws Exception {
        String requestBody = """
                {
                  "accountNumber": "12345678",
                  "sortCode": "12-34-56",
                  "reductionAmount": 0
                }
                """;

        mockMvc.perform(post("/api/reduction-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenAccountNumberIsInvalid() throws Exception {
        String requestBody = """
                {
                  "accountNumber": "123",
                  "sortCode": "12-34-56",
                  "reductionAmount": 500.00
                }
                """;

        mockMvc.perform(post("/api/reduction-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenSortCodeIsInvalid() throws Exception {
        String requestBody = """
                {
                  "accountNumber": "12345678",
                  "sortCode": "123456",
                  "reductionAmount": 500.00
                }
                """;

        mockMvc.perform(post("/api/reduction-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenRequiredFieldIsMissing() throws Exception {
        String requestBody = """
                {
                  "accountNumber": "12345678",
                  "sortCode": "12-34-56"
                }
                """;

        mockMvc.perform(post("/api/reduction-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnLatestReductionPlanForAccount() throws Exception {
        ReductionPlanEntity olderPlan = new ReductionPlanEntity(
                UUID.randomUUID(),
                "12345678",
                "12-34-56",
                new BigDecimal("300.00"),
                ReductionPlanStatus.ACTIVE,
                Instant.parse("2026-06-17T09:00:00Z")
        );

        ReductionPlanEntity latestPlan = new ReductionPlanEntity(
                UUID.randomUUID(),
                "12345678",
                "12-34-56",
                new BigDecimal("500.00"),
                ReductionPlanStatus.ACTIVE,
                Instant.parse("2026-06-17T10:00:00Z")
        );

        ReductionPlanEntity differentAccountPlan = new ReductionPlanEntity(
                UUID.randomUUID(),
                "87654321",
                "12-34-56",
                new BigDecimal("900.00"),
                ReductionPlanStatus.ACTIVE,
                Instant.parse("2026-06-17T11:00:00Z")
        );

        repository.save(olderPlan);
        repository.save(latestPlan);
        repository.save(differentAccountPlan);

        mockMvc.perform(get("/api/reduction-plans/latest")
                        .param("accountNumber", "12345678")
                        .param("sortCode", "12-34-56"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planId", is(latestPlan.getPlanId().toString())))
                .andExpect(jsonPath("$.accountNumber", is("12345678")))
                .andExpect(jsonPath("$.sortCode", is("12-34-56")))
                .andExpect(jsonPath("$.reductionAmount").value(500.00))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.createdAt", is("2026-06-17T10:00:00Z")));
    }

    @Test
    void shouldReturnNotFoundWhenNoReductionPlanExistsForAccount() throws Exception {
        mockMvc.perform(get("/api/reduction-plans/latest")
                        .param("accountNumber", "12345678")
                        .param("sortCode", "12-34-56"))
                .andExpect(status().isNotFound());
    }
}