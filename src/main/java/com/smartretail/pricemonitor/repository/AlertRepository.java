package com.smartretail.pricemonitor.repository;

import com.smartretail.pricemonitor.entity.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    Page<Alert> findByUserId(Long userId, Pageable pageable);
    List<Alert> findByCommodityIdAndMarketIdAndIsActiveTrue(Long commodityId, Long marketId);
    List<Alert> findByIsActiveTrue();
}
