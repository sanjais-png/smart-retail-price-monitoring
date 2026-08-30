package com.smartretail.pricemonitor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommodityRequest {
    @NotBlank(message = "Commodity name is required")
    private String name;

    @NotBlank(message = "Commodity code is required")
    private String code;

    private String description;

    @NotBlank(message = "Unit of measurement is required (e.g. kg, liter)")
    private String unit;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private String imagePath;
}
