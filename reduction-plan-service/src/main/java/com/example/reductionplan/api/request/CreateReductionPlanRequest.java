package com.example.reductionplan.api.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record CreateReductionPlanRequest(
        @NotBlank(message = "Account Number is required")
        @Pattern(regexp = "\\d{8}", message = "Account Number must be 8 digits")
        String accountNumber,

        @NotBlank(message = "Sort Code is required")
        @Pattern(regexp = "\\d{2}-\\d{2}-\\d{2}", message = "Sort Code must match format XX-XX-XX")
        String sortCode,

        @NotNull(message = "Reduction Amount is required")
        @DecimalMin(value = "0.01", message = "Reduction Amount must be greater than zero")
        BigDecimal reductionAmount
) {
}