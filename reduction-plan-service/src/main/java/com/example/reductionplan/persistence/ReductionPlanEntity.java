package com.example.reductionplan.persistence;

import com.example.reductionplan.domain.ReductionPlanStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reduction_plans")
public class ReductionPlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID planId;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String sortCode;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal reductionAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReductionPlanStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    protected ReductionPlanEntity() {
    }

    public ReductionPlanEntity(
            UUID planId,
            String accountNumber,
            String sortCode,
            BigDecimal reductionAmount,
            ReductionPlanStatus status,
            Instant createdAt
    ) {
        this.planId = planId;
        this.accountNumber = accountNumber;
        this.sortCode = sortCode;
        this.reductionAmount = reductionAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public UUID getPlanId() {
        return planId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getSortCode() {
        return sortCode;
    }

    public BigDecimal getReductionAmount() {
        return reductionAmount;
    }

    public ReductionPlanStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}