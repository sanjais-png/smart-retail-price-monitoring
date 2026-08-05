package com.smartretail.pricemonitor.repository;

import com.smartretail.pricemonitor.constants.PredictionTimeframe;
import com.smartretail.pricemonitor.entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    List<Prediction> findByCommodityIdAndMarketIdOrderByTargetDateDesc(Long commodityId, Long marketId);
    Optional<Prediction> findByCommodityIdAndMarketIdAndTimeframe(Long commodityId, Long marketId, PredictionTimeframe timeframe);
}
