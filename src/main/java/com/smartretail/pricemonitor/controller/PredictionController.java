package com.smartretail.pricemonitor.controller;

import com.smartretail.pricemonitor.dto.ApiResponse;
import com.smartretail.pricemonitor.dto.PredictionRequest;
import com.smartretail.pricemonitor.dto.PredictionResponse;
import com.smartretail.pricemonitor.service.PredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/predictions")
@RequiredArgsConstructor
@Tag(name = "AI Price Predictions", description = "AI-powered price forecasting for Tomorrow, Next Week, Next Month")
@SecurityRequirement(name = "bearerAuth")
public class PredictionController {

    private final PredictionService predictionService;

    @PostMapping("/generate")
    @Operation(summary = "Generate AI price prediction for a specific timeframe",
               description = "Uses exponential smoothing and linear regression to predict commodity prices. Returns confidence score.")
    public ResponseEntity<ApiResponse<PredictionResponse>> generatePrediction(
            @Valid @RequestBody PredictionRequest request) {
        PredictionResponse prediction = predictionService.generatePrediction(request);
        return ResponseEntity.ok(ApiResponse.success("Price prediction generated successfully", prediction));
    }

    @GetMapping
    @Operation(summary = "Get all predictions for a commodity in a specific market")
    public ResponseEntity<ApiResponse<List<PredictionResponse>>> getPredictions(
            @RequestParam Long commodityId,
            @RequestParam Long marketId) {
        return ResponseEntity.ok(ApiResponse.success("Predictions retrieved",
                predictionService.getPredictionsByCommodityAndMarket(commodityId, marketId)));
    }
}
