package com.smartretail.pricemonitor.service;

import com.smartretail.pricemonitor.constants.ComplaintStatus;
import com.smartretail.pricemonitor.dto.ComplaintRequest;
import com.smartretail.pricemonitor.dto.ComplaintResponse;
import com.smartretail.pricemonitor.dto.PagedResponse;

public interface ComplaintService {
    ComplaintResponse submitComplaint(String username, ComplaintRequest request);
    ComplaintResponse updateComplaintStatus(Long complaintId, ComplaintStatus status,
                                           String resolutionNotes, String authorityUsername);
    ComplaintResponse getComplaintById(Long id);
    ComplaintResponse getComplaintById(Long id, String principalName, boolean isPrivileged);
    PagedResponse<ComplaintResponse> getUserComplaints(String username, int page, int size);
    PagedResponse<ComplaintResponse> getComplaintsByStatus(ComplaintStatus status, int page, int size);
    PagedResponse<ComplaintResponse> getAllComplaints(int page, int size);
}
