package com.example.reductionplan.messaging;

import com.example.reductionplan.domain.ReductionPlanEvent;

public interface ReductionPlanEventPublisher {

    void publish(ReductionPlanEvent event);
}