package com.smartretail.pricemonitor.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "market_price_observations")
@CompoundIndexes({
    @CompoundIndex(name = "uniq_source_record_idx", def = "{'source': 1, 'sourceRecordId': 1}", unique = true),
    @CompoundIndex(name = "com_mkt_date_idx", def = "{'commodityId': 1, 'marketId': 1, 'observedAt': -1}"),
    @CompoundIndex(name = "location_date_idx", def = "{'state': 1, 'district': 1, 'observedAt': -1}")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketPriceObservation {

    @Id
    private String id;

    @Field("source")
    private String source;

    @Field("source_record_id")
    private String sourceRecordId;

    @Indexed
    @Field("commodity_id")
    private Long commodityId;

    @Field("commodity_code")
    private String commodityCode;

    @Field("commodity_name")
    private String commodityName;

    @Indexed
    @Field("market_id")
    private Long marketId;

    @Field("market_code")
    private String marketCode;

    @Field("market_name")
    private String marketName;

    @Field("state")
    private String state;

    @Field("district")
    private String district;

    @Field("city")
    private String city;

    @Field("min_price")
    private BigDecimal minPrice;

    @Field("max_price")
    private BigDecimal maxPrice;

    @Field("modal_price")
    private BigDecimal modalPrice;

    @Field("unit")
    private String unit;

    @Field("observed_at")
    private Instant observedAt;

    @Field("ingested_at")
    private Instant ingestedAt;

    @Builder.Default
    @Field("metadata")
    private Map<String, Object> metadata = new HashMap<>();
}
