package com.smartretail.pricemonitor.repository;

import com.smartretail.pricemonitor.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    List<PriceHistory> findByCommodityIdAndMarketIdAndRecordedDateBetweenOrderByRecordedDateAsc(
        Long commodityId, Long marketId, LocalDate startDate, LocalDate endDate
    );

    @Query("SELECT ph FROM PriceHistory ph WHERE ph.commodity.id = :commodityId AND ph.market.id = :marketId ORDER BY ph.recordedDate DESC LIMIT 1")
    Optional<PriceHistory> findLatestPrice(@Param("commodityId") Long commodityId, @Param("marketId") Long marketId);

    @Query("SELECT AVG(ph.averagePrice) FROM PriceHistory ph WHERE ph.commodity.id = :commodityId")
    Optional<BigDecimal> findAveragePriceNationwide(@Param("commodityId") Long commodityId);
}
