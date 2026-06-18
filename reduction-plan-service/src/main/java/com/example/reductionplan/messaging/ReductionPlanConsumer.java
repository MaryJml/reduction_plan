package com.example.reductionplan.messaging;

import com.example.reductionplan.domain.ReductionPlanEvent;
import com.example.reductionplan.service.ReductionPlanProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "app.messaging.consumer.enabled",
        havingValue = "true"
)
public class ReductionPlanConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReductionPlanConsumer.class);

    private final ReductionPlanProcessor processor;

    public ReductionPlanConsumer(ReductionPlanProcessor processor) {
        this.processor = processor;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.reduction-plan-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(ReductionPlanEvent event) {
        LOGGER.info(
                "Reduction plan event received. eventId={}, planId={}, eventType={}, status={}",
                event.eventId(),
                event.planId(),
                event.eventType(),
                event.status()
        );

        processor.process(event);

        LOGGER.info(
                "Reduction plan event processed. eventId={}, planId={}",
                event.eventId(),
                event.planId()
        );
    }
}