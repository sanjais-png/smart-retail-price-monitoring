package com.smartretail.pricemonitor.constants;

public enum ApprovalStatus {
    ACTIVE,             // Standard active consumer account
    PENDING_AUTHORITY,  // Awaiting Admin review for Authority privileges
    APPROVED,           // Authority access approved by Admin
    DENIED              // Authority access denied by Admin
}
