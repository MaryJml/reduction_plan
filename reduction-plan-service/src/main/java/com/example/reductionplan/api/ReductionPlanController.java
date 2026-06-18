package com.example.reductionplan.api;

import com.example.reductionplan.api.request.CreateReductionPlanRequest;
import com.example.reductionplan.api.response.CreateReductionPlanResponse;
import com.example.reductionplan.api.response.ReductionPlanResponse;
import com.example.reductionplan.service.ReductionPlanCommandService;
import com.example.reductionplan.service.ReductionPlanQueryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reduction-plans")
public class ReductionPlanController {

    private final ReductionPlanCommandService commandService;
    private final ReductionPlanQueryService queryService;

    public ReductionPlanController(
            ReductionPlanCommandService commandService,
            ReductionPlanQueryService queryService
    ) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<CreateReductionPlanResponse> submitReductionPlan(
            @Valid @RequestBody CreateReductionPlanRequest request
    ) {
        CreateReductionPlanResponse response = commandService.submit(request);
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/latest")
    public ResponseEntity<ReductionPlanResponse> getLatestReductionPlan(
            @RequestParam String accountNumber,
            @RequestParam String sortCode
    ) {
        ReductionPlanResponse response = queryService.getLatest(accountNumber, sortCode);
        return ResponseEntity.ok(response);
    }
}