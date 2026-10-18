package com.flashflow.controller;

import com.flashflow.dto.StressTestRequest;
import com.flashflow.dto.StressTestResult;
import com.flashflow.service.StressTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stress-test")
@Tag(name = "Stress Testing & Benchmarking", description = "Endpoints for simulating high-concurrency race conditions")
public class StressTestController {

    private final StressTestService stressTestService;

    public StressTestController(StressTestService stressTestService) {
        this.stressTestService = stressTestService;
    }

    @PostMapping("/run")
    @Operation(summary = "Execute real-time concurrency benchmark simulating 50-100 parallel buyers")
    public ResponseEntity<StressTestResult> runStressTest(@RequestBody StressTestRequest request) {
        StressTestResult result = stressTestService.runConcurrencyStressTest(request);
        return ResponseEntity.ok(result);
    }
}
