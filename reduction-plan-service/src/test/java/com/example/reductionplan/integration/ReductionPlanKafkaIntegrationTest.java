package com.example.reductionplan.integration;

import com.example.reductionplan.persistence.ProcessedEventRepository;
import com.example.reductionplan.persistence.ReductionPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(properties = {
        "app.messaging.publisher=kafka",
        "app.messaging.consumer.enabled=true",
        "spring.kafka.consumer.group-id=reduction-plan-service-integration-test",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
@AutoConfigureMockMvc
class ReductionPlanKafkaIntegrationTest {

    @Container
    static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("apache/kafka:3.7.0")
    );

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReductionPlanRepository reductionPlanRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @BeforeEach
    void setUp() {
        processedEventRepository.deleteAll();
        reductionPlanRepository.deleteAll();
    }

    @Test
    void shouldSubmitReductionPlanPublishKafkaEventConsumeEventPersistPlanAndReturnLatestPlan() throws Exception {
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

        await()
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> {
                    mockMvc.perform(get("/api/reduction-plans/latest")
                                    .param("accountNumber", "12345678")
                                    .param("sortCode", "12-34-56"))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.planId", notNullValue()))
                            .andExpect(jsonPath("$.accountNumber", is("12345678")))
                            .andExpect(jsonPath("$.sortCode", is("12-34-56")))
                            .andExpect(jsonPath("$.reductionAmount", is(500.0)))
                            .andExpect(jsonPath("$.status", is("ACTIVE")))
                            .andExpect(jsonPath("$.createdAt", notNullValue()));

                    assertThat(reductionPlanRepository.findAll()).hasSize(1);
                    assertThat(processedEventRepository.findAll()).hasSize(1);
                });
    }
}