package com.example.reductionplan.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(
        name = "app.messaging.publisher",
        havingValue = "kafka"
)
public class KafkaTopicConfig {

    @Bean
    public NewTopic reductionPlanEventsTopic(
            @Value("${app.kafka.topics.reduction-plan-events}") String topicName
    ) {
        return TopicBuilder
                .name(topicName)
                .partitions(1)
                .replicas(1)
                .build();
    }
}