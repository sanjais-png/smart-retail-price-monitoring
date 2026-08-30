package com.smartretail.pricemonitor.repository.mongo;

import com.smartretail.pricemonitor.document.MarketPriceObservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarketPriceObservationRepository extends MongoRepository<MarketPriceObservation, String> {

    boolean existsBySourceAndSourceRecordId(String source, String sourceRecordId);

    Optional<MarketPriceObservation> findTopByCommodityIdAndMarketIdOrderByObservedAtDesc(Long commodityId, Long marketId);

    Optional<MarketPriceObservation> findTopByCommodityIdOrderByObservedAtDesc(Long commodityId);

    Page<MarketPriceObservation> findByCommodityIdAndMarketIdAndObservedAtBetween(
            Long commodityId, Long marketId, Instant start, Instant end, Pageable pageable);

    List<MarketPriceObservation> findByCommodityIdAndObservedAtAfter(Long commodityId, Instant after, Sort sort);

    List<MarketPriceObservation> findByStateAndDistrict(String state, String district, Pageable pageable);
}
