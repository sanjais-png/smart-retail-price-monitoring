package com.smartretail.pricemonitor.repository;

import com.smartretail.pricemonitor.constants.ComplaintStatus;
import com.smartretail.pricemonitor.entity.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByUserId(Long userId);
    Page<Complaint> findByUserId(Long userId, Pageable pageable);
    Page<Complaint> findByStatus(ComplaintStatus status, Pageable pageable);
    long countByStatus(ComplaintStatus status);
}
