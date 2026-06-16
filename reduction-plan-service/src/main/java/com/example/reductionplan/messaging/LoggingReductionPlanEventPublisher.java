package com.example.reductionplan.messaging;

import com.example.reductionplan.domain.ReductionPlanEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingReductionPlanEventPublisher implements ReductionPlanEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingReductionPlanEventPublisher.class);

    @Override
    public void publish(ReductionPlanEvent event) {
        LOGGER.info(
                "Reduction plan event published. eventId={}, planId={}, eventType={}, status={}",
                event.eventId(),
                event.planId(),
                event.eventType(),
                event.status()
        );
    }
}