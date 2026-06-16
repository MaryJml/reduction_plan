package com.example.reductionplan.api;

import com.example.reductionplan.api.request.CreateReductionPlanRequest;
import com.example.reductionplan.api.response.CreateReductionPlanResponse;
import com.example.reductionplan.service.ReductionPlanCommandService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reduction-plans")
public class ReductionPlanController {

    private final ReductionPlanCommandService commandService;

    public ReductionPlanController(ReductionPlanCommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping
    public ResponseEntity<CreateReductionPlanResponse> submitReductionPlan(
            @Valid @RequestBody CreateReductionPlanRequest request
    ) {
        CreateReductionPlanResponse response = commandService.submit(request);
        return ResponseEntity.accepted().body(response);
    }
}