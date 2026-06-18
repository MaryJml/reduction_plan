package com.example.reductionplan.service;

import com.example.reductionplan.api.response.ReductionPlanResponse;
import com.example.reductionplan.exception.ReductionPlanNotFoundException;
import com.example.reductionplan.persistence.ReductionPlanEntity;
import com.example.reductionplan.persistence.ReductionPlanRepository;
import org.springframework.stereotype.Service;

@Service
public class ReductionPlanQueryService {

    private final ReductionPlanRepository repository;

    public ReductionPlanQueryService(ReductionPlanRepository repository) {
        this.repository = repository;
    }

    public ReductionPlanResponse getLatest(String accountNumber, String sortCode) {
        ReductionPlanEntity entity = repository
                .findTopByAccountNumberAndSortCodeOrderByCreatedAtDesc(accountNumber, sortCode)
                .orElseThrow(() -> new ReductionPlanNotFoundException(accountNumber, sortCode));

        return ReductionPlanResponse.from(entity);
    }
}