package com.example.reductionplan.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ReductionPlanNotFoundException extends RuntimeException {

    public ReductionPlanNotFoundException(String accountNumber, String sortCode) {
        super(String.format(
                "No reduction plan found for accountNumber=%s and sortCode=%s",
                accountNumber,
                sortCode
        ));
    }
}