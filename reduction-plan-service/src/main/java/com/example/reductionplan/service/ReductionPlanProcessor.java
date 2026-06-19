package com.example.reductionplan.service;

import com.example.reductionplan.domain.ReductionPlanEvent;
import com.example.reductionplan.domain.ReductionPlanStatus;
import com.example.reductionplan.persistence.ProcessedEventEntity;
import com.example.reductionplan.persistence.ProcessedEventRepository;
import com.example.reductionplan.persistence.ReductionPlanEntity;
import com.example.reductionplan.persistence.ReductionPlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class ReductionPlanProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReductionPlanProcessor.class);

    private final ReductionPlanRepository reductionPlanRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final Clock clock;

    public ReductionPlanProcessor(
            ReductionPlanRepository reductionPlanRepository,
            ProcessedEventRepository processedEventRepository,
            Clock clock
    ) {
        this.reductionPlanRepository = reductionPlanRepository;
        this.processedEventRepository = processedEventRepository;
        this.clock = clock;
    }

    @Transactional
    public void process(ReductionPlanEvent event) {
        if (processedEventRepository.existsByEventId(event.eventId())) {
            LOGGER.info(
                    "Duplicate reduction plan event ignored. eventId={}, planId={}",
                    event.eventId(),
                    event.planId()
            );
            return;
        }

        ReductionPlanEntity entity = new ReductionPlanEntity(
                event.planId(),
                event.accountNumber(),
                event.sortCode(),
                event.reductionAmount(),
                ReductionPlanStatus.ACTIVE,
                event.occurredAt()
        );

        reductionPlanRepository.save(entity);

        ProcessedEventEntity processedEvent = new ProcessedEventEntity(
                event.eventId(),
                Instant.now(clock)
        );

        processedEventRepository.save(processedEvent);
    }
}