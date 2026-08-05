package com.smartretail.pricemonitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommodityResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String unit;
    private String imagePath;
    private Long categoryId;
    private String categoryName;
    private boolean active;
    private LocalDateTime createdAt;
}
