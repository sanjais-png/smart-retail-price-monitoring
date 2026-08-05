package com.smartretail.pricemonitor.entity;

import com.smartretail.pricemonitor.constants.TrendDirection;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "market_trends")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketTrend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commodity_id", nullable = false)
    private Commodity commodity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_id", nullable = false)
    private Market market;

    @Column(name = "period_type", nullable = false, length = 20) // DAILY, WEEKLY, MONTHLY, YEARLY
    private String periodType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "avg_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal avgPrice;

    @Column(name = "price_change_percentage")
    private Double priceChangePercentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "trend_direction", length = 20)
    private TrendDirection trendDirection;
}
