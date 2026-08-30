package com.smartretail.pricemonitor.entity;

import com.smartretail.pricemonitor.constants.FairnessStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fairness_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FairnessResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commodity_id", nullable = false)
    private Commodity commodity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_id", nullable = false)
    private Market market;

    @Column(name = "purchase_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "market_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal marketPrice;

    @Column(name = "price_difference", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceDifference;

    @Column(name = "percentage_difference", nullable = false)
    private Double percentageDifference;

    @Column(name = "fairness_score", nullable = false)
    private Integer fairnessScore; // 0 - 100

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private FairnessStatus status;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "is_cold_start")
    private Boolean isColdStart;

    @Column(name = "baseline_source", length = 150)
    private String baselineSource;

    @Column(name = "reference_source", length = 100)
    private String referenceSource;

    @Column(name = "reference_observed_at")
    private java.time.Instant referenceObservedAt;

    @Column(name = "reference_observation_id", length = 100)
    private String referenceObservationId;

    @CreationTimestamp
    @Column(name = "calculated_at", updatable = false)
    private LocalDateTime calculatedAt;
}
