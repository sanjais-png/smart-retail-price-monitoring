package com.smartretail.pricemonitor.repository;

import com.smartretail.pricemonitor.constants.ReportStatus;
import com.smartretail.pricemonitor.entity.PriceReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceReportRepository extends JpaRepository<PriceReport, Long> {
    Page<PriceReport> findByUserId(Long userId, Pageable pageable);
    Page<PriceReport> findByStatus(ReportStatus status, Pageable pageable);
    List<PriceReport> findByCommodityIdAndMarketId(Long commodityId, Long marketId);
    List<PriceReport> findByCommodityIdAndMarketIdAndStatus(Long commodityId, Long marketId, ReportStatus status);

    @Query("SELECT AVG(pr.price) FROM PriceReport pr WHERE pr.commodity.id = :commodityId AND pr.market.id = :marketId AND pr.status = 'VERIFIED'")
    Optional<BigDecimal> findAverageVerifiedPrice(@Param("commodityId") Long commodityId, @Param("marketId") Long marketId);

    long countByStatus(ReportStatus status);
}
