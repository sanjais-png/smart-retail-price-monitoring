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
public class SystemSettingDto {
    private Long id;
    private String settingKey;
    private String settingValue;
    private String description;
    private boolean isEditable;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
