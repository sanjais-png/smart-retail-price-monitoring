package com.smartretail.pricemonitor.repository;

import com.smartretail.pricemonitor.entity.FairnessResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FairnessResultRepository extends JpaRepository<FairnessResult, Long> {
    Page<FairnessResult> findByUserId(Long userId, Pageable pageable);
}
