package com.smartretail.pricemonitor.dto;

import com.smartretail.pricemonitor.constants.ComplaintStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintResponse {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private Long marketId;
    private String marketName;
    private Long commodityId;
    private String commodityName;
    private String title;
    private String description;
    private ComplaintStatus status;
    private String assignedAuthorityUsername;
    private String resolutionNotes;
    private LocalDateTime createdAt;
}
