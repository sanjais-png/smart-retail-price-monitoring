package com.smartretail.pricemonitor.repository;

import com.smartretail.pricemonitor.entity.MarketTrend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketTrendRepository extends JpaRepository<MarketTrend, Long> {
    List<MarketTrend> findByCommodityIdAndMarketIdAndPeriodTypeOrderByStartDateDesc(
        Long commodityId, Long marketId, String periodType
    );
}
