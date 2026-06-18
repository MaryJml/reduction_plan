package com.example.reductionplan.messaging;

import com.example.reductionplan.domain.ReductionPlanEvent;
import com.example.reductionplan.exception.EventPublishingException;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.common.KafkaException;

@Component
@ConditionalOnProperty(
        name = "app.messaging.publisher",
        havingValue = "kafka"
)
public class KafkaReductionPlanEventPublisher implements ReductionPlanEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaReductionPlanEventPublisher.class);
    private static final long SEND_TIMEOUT_SECONDS = 5;

    private final KafkaTemplate<String, ReductionPlanEvent> kafkaTemplate;
    private final String topicName;

    public KafkaReductionPlanEventPublisher(
            KafkaTemplate<String, ReductionPlanEvent> kafkaTemplate,
            @Value("${app.kafka.topics.reduction-plan-events}") String topicName
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    @Override
    public void publish(ReductionPlanEvent event) {
        String messageKey = buildMessageKey(event);

        try {
            SendResult<String, ReductionPlanEvent> sendResult = kafkaTemplate
                    .send(topicName, messageKey, event)
                    .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            RecordMetadata metadata = sendResult.getRecordMetadata();

            LOGGER.info(
                    "Reduction plan event sent to Kafka. eventId={}, planId={}, topic={}, partition={}, offset={}",
                    event.eventId(),
                    event.planId(),
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset()
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new EventPublishingException("Interrupted while publishing reduction plan event", exception);
        } catch (ExecutionException | TimeoutException | KafkaException exception) {
            throw new EventPublishingException("Failed to publish reduction plan event to Kafka", exception);
        }
    }

    private String buildMessageKey(ReductionPlanEvent event) {
        return event.accountNumber() + "-" + event.sortCode();
    }
}