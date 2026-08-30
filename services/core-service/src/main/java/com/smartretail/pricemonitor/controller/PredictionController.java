package com.smartretail.pricemonitor.controller;

import com.smartretail.pricemonitor.constants.PredictionTimeframe;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/predictions")
@RequiredArgsConstructor
@Tag(name = "AI Price Predictions", description = "AI-powered time-series price forecasting for Tomorrow, Next Week, Next Month")
@SecurityRequirement(name = "bearerAuth")
public class PredictionController {

    private final PredictionService predictionService;

    @PostMapping("/generate")
    @Operation(summary = "Generate AI price prediction for a specific timeframe",
               description = "Uses Holt-Winters exponential smoothing & lagged regression with walk-forward validation")
    public ResponseEntity<ApiResponse<PredictionResponse>> generatePrediction(
            @Valid @RequestBody PredictionRequest request) {
        PredictionResponse prediction = predictionService.generatePrediction(request);
        return ResponseEntity.ok(ApiResponse.success("Price prediction generated successfully", prediction));
    }

    @GetMapping("/forecast/{commodityId}")
    @Operation(summary = "Get multi-horizon time-series price forecast (Tomorrow, 7-Day, 30-Day)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMultiHorizonForecast(
            @PathVariable Long commodityId,
            @RequestParam(required = false, defaultValue = "1") Long marketId) {

        PredictionRequest tomReq = new PredictionRequest(commodityId, marketId, PredictionTimeframe.TOMORROW);
        PredictionRequest weekReq = new PredictionRequest(commodityId, marketId, PredictionTimeframe.NEXT_WEEK);
        PredictionRequest monthReq = new PredictionRequest(commodityId, marketId, PredictionTimeframe.NEXT_MONTH);

        PredictionResponse tomRes = predictionService.generatePrediction(tomReq);
        PredictionResponse weekRes = predictionService.generatePrediction(weekReq);
        PredictionResponse monthRes = predictionService.generatePrediction(monthReq);

        Map<String, Object> response = new HashMap<>();
        response.put("commodityId", commodityId);
        response.put("commodityName", tomRes.getCommodityName());
        response.put("marketId", marketId);
        response.put("marketName", tomRes.getMarketName());
        response.put("tomorrow", tomRes);
        response.put("sevenDay", weekRes);
        response.put("thirtyDay", monthRes);
        response.put("forecastEngine", tomRes.getModelVersion());

        return ResponseEntity.ok(ApiResponse.success("Multi-horizon price forecast generated", response));
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
