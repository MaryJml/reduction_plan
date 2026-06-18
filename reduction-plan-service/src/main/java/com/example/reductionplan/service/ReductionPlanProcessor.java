package com.example.reductionplan.service;

import com.example.reductionplan.domain.ReductionPlanEvent;
import com.example.reductionplan.domain.ReductionPlanStatus;
import com.example.reductionplan.persistence.ReductionPlanEntity;
import com.example.reductionplan.persistence.ReductionPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReductionPlanProcessor {

    private final ReductionPlanRepository repository;

    public ReductionPlanProcessor(ReductionPlanRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void process(ReductionPlanEvent event) {
        ReductionPlanEntity entity = new ReductionPlanEntity(
                event.planId(),
                event.accountNumber(),
                event.sortCode(),
                event.reductionAmount(),
                ReductionPlanStatus.ACTIVE,
                event.occurredAt()
        );

        repository.save(entity);
    }
}