package com.smartretail.pricemonitor.service;

import com.smartretail.pricemonitor.dto.AuditLogResponse;
import com.smartretail.pricemonitor.dto.PagedResponse;

public interface AuditLogService {
    void log(String username, String action, String entityName, Long entityId, String details);
    PagedResponse<AuditLogResponse> getAllLogs(int page, int size);
    PagedResponse<AuditLogResponse> getLogsByUser(Long userId, int page, int size);
}
