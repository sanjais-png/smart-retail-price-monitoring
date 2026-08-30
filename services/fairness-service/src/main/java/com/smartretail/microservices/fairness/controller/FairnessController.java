package com.smartretail.microservices.fairness.controller;

import com.smartretail.pricemonitor.dto.ApiResponse;
import com.smartretail.pricemonitor.dto.FairnessCheckRequest;
import com.smartretail.pricemonitor.dto.FairnessCheckResponse;
import com.smartretail.pricemonitor.service.FairnessEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fairness")
@RequiredArgsConstructor
@Tag(name = "Fairness Engine", description = "Consumer price fairness evaluation with fairness score (0-100)")
@SecurityRequirement(name = "bearerAuth")
public class FairnessController {

    private final FairnessEngineService fairnessEngineService;

    @PostMapping("/evaluate")
    @Operation(summary = "Evaluate purchase price fairness against market baseline",
               description = "Calculates fairness score, percentage difference, and provides recommendation.\n\n" +
                             "**Location Selection Modes:**\n" +
                             "1. **Direct Market ID (Optional):** Pass `marketId` if selected from market list.\n" +
                             "2. **Shop/City Input (Optional):** Pass `marketName` + `city` + `state` for remote purchases (e.g. bought in Chennai, checking from Coimbatore).\n" +
                             "3. **GPS Auto-Resolution (Optional):** Pass `latitude` + `longitude`.\n\n" +
                             "Status: FAIR (<=5%), SLIGHTLY_HIGH (5-15%), HIGH (15-30%), VERY_HIGH (>30%), UNDERPRICED (<-5%)")
    public ResponseEntity<ApiResponse<FairnessCheckResponse>> evaluateFairness(
            @Valid @RequestBody FairnessCheckRequest request,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        FairnessCheckResponse result = fairnessEngineService.evaluateFairness(username, request);
        return ResponseEntity.ok(ApiResponse.success("Fairness evaluation completed", result));
    }
}
