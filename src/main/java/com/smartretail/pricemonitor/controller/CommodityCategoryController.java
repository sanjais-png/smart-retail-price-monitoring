package com.smartretail.pricemonitor.controller;

import com.smartretail.pricemonitor.dto.ApiResponse;
import com.smartretail.pricemonitor.dto.CommodityCategoryResponse;
import com.smartretail.pricemonitor.service.CommodityCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Commodity Categories", description = "Commodity category management (Vegetables, Fruits, Grains, etc.)")
@SecurityRequirement(name = "bearerAuth")
public class CommodityCategoryController {

    private final CommodityCategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all commodity categories")
    public ResponseEntity<ApiResponse<List<CommodityCategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully",
                categoryService.getAllCategories()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ApiResponse<CommodityCategoryResponse>> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Category retrieved", categoryService.getCategoryById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new commodity category (Admin only)")
    public ResponseEntity<ApiResponse<CommodityCategoryResponse>> createCategory(
            @RequestParam String name,
            @RequestParam(required = false) String description) {
        CommodityCategoryResponse category = categoryService.createCategory(name, description);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", category));
    }
}
