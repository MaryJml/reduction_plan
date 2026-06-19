package com.example.reductionplan.exception;

public class ReductionPlanNotFoundException extends RuntimeException {

    public ReductionPlanNotFoundException(String accountNumber, String sortCode) {
        super("No reduction plan found for the requested account");
    }
}