package com.smartretail.pricemonitor.prediction;

import com.smartretail.pricemonitor.document.MarketPriceObservation;
import com.smartretail.pricemonitor.entity.PriceHistory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Component
public class PriceDataPreprocessor {

    public static record NormalizedObservation(
            LocalDate date,
            BigDecimal price,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String source
    ) {}

    /**
     * Deduplicates, sorts, normalizes, and aligns historical observations
     * from both MySQL PriceHistory and MongoDB MarketPriceObservation sources.
     * Preserves legitimate price spikes vital for spatial price-gouging detection.
     */
    public List<NormalizedObservation> preprocess(List<PriceHistory> mysqlHistory, List<MarketPriceObservation> mongoObs) {
        Map<LocalDate, NormalizedObservation> map = new HashMap<>();

        // 1. Ingest MySQL records
        if (mysqlHistory != null) {
            for (PriceHistory ph : mysqlHistory) {
                if (ph.getRecordedDate() != null && ph.getAveragePrice() != null && ph.getAveragePrice().compareTo(BigDecimal.ZERO) > 0) {
                    LocalDate d = ph.getRecordedDate();
                    map.put(d, new NormalizedObservation(
                            d,
                            ph.getAveragePrice(),
                            ph.getMinPrice() != null ? ph.getMinPrice() : ph.getAveragePrice(),
                            ph.getMaxPrice() != null ? ph.getMaxPrice() : ph.getAveragePrice(),
                            ph.getSource() != null ? ph.getSource() : "MYSQL"
                    ));
                }
            }
        }

        // 2. Ingest MongoDB records (Deduplicate: override if mongo record is more recent or non-existent)
        if (mongoObs != null) {
            for (MarketPriceObservation obs : mongoObs) {
                if (obs.getObservedAt() != null && obs.getModalPrice() != null && obs.getModalPrice().compareTo(BigDecimal.ZERO) > 0) {
                    LocalDate d = obs.getObservedAt().atZone(ZoneId.systemDefault()).toLocalDate();
                    if (!map.containsKey(d)) {
                        map.put(d, new NormalizedObservation(
                                d,
                                obs.getModalPrice(),
                                obs.getMinPrice() != null ? obs.getMinPrice() : obs.getModalPrice(),
                                obs.getMaxPrice() != null ? obs.getMaxPrice() : obs.getModalPrice(),
                                "MONGODB"
                        ));
                    }
                }
            }
        }

        // 3. Sort chronologically
        List<NormalizedObservation> sorted = new ArrayList<>(map.values());
        sorted.sort(Comparator.comparing(NormalizedObservation::date));

        // 4. Clean corrupted records while strictly preserving legitimate market price spikes
        List<NormalizedObservation> cleaned = new ArrayList<>();
        for (NormalizedObservation obs : sorted) {
            double val = obs.price().doubleValue();
            // Discard invalid <= 0 or extreme corrupt data (> 10000/kg)
            if (val > 0.0 && val < 10000.0) {
                cleaned.add(obs);
            }
        }

        return cleaned;
    }
}
