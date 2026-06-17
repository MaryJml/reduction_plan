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

@SpringBootTest
@AutoConfigureMockMvc
class ReductionPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
}