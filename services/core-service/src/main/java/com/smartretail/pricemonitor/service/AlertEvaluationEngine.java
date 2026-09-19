package com.smartretail.pricemonitor.service;

import com.smartretail.pricemonitor.constants.AlertType;
import com.smartretail.pricemonitor.entity.Alert;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AlertEvaluationEngine {

    public static class EvaluationResult {
        private final boolean triggered;
        private final BigDecimal normalizedPrice;
        private final String message;

        public EvaluationResult(boolean triggered, BigDecimal normalizedPrice, String message) {
            this.triggered = triggered;
            this.normalizedPrice = normalizedPrice;
            this.message = message;
        }

        public boolean isTriggered() { return triggered; }
        public BigDecimal getNormalizedPrice() { return normalizedPrice; }
        public String getMessage() { return message; }
    }

    /**
     * Converts raw observed price to commodity's target unit if possible.
     * Returns null if units are incompatible.
     */
    public static BigDecimal normalizePrice(BigDecimal rawPrice, String observedUnit, String targetUnit) {
        if (rawPrice == null) return null;
        if (observedUnit == null || targetUnit == null) return rawPrice;

        String obs = observedUnit.trim().toLowerCase();
        String tgt = targetUnit.trim().toLowerCase();

        if (obs.equals(tgt) || obs.contains(tgt) || tgt.contains(obs)) {
            return rawPrice;
        }

        // Quintal (100 kg) to kg
        if ((obs.contains("quintal") || obs.equals("qtl")) && (tgt.contains("kg") || tgt.equals("kilo"))) {
            return rawPrice.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        // kg to Quintal
        if ((obs.contains("kg") || obs.equals("kilo")) && (tgt.contains("quintal") || tgt.equals("qtl"))) {
            return rawPrice.multiply(BigDecimal.valueOf(100));
        }

        // Gram to kg
        if (obs.contains("gram") || obs.equals("g")) {
            if (tgt.contains("kg")) return rawPrice.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
        }

        // Liter vs Kg mismatch
        if ((obs.contains("liter") || obs.contains("ltr")) != (tgt.contains("liter") || tgt.contains("ltr"))) {
            return null;
        }

        return null;
    }

    public static EvaluationResult evaluate(Alert alert, BigDecimal rawPrice, String observedUnit) {
        if (alert == null || !alert.isActive() || rawPrice == null) {
            return new EvaluationResult(false, null, null);
        }

        String targetUnit = alert.getCommodity() != null ? alert.getCommodity().getUnit() : null;
        BigDecimal currentPrice = normalizePrice(rawPrice, observedUnit, targetUnit);

        if (currentPrice == null) {
            return new EvaluationResult(false, null, "Incompatible price unit: " + observedUnit + " vs " + targetUnit);
        }

        BigDecimal targetPrice = alert.getTargetPrice();
        BigDecimal lastEval = alert.getLastEvaluatedPrice();
        BigDecimal lastTrig = alert.getLastTriggeredPrice();
        AlertType type = alert.getAlertType();

        boolean shouldTrigger = false;
        String message = "";

        switch (type) {
            case PRICE_DECREASE:
                if (currentPrice.compareTo(targetPrice) <= 0) {
                    if (lastTrig == null || (lastEval != null && lastEval.compareTo(targetPrice) > 0) || currentPrice.compareTo(lastTrig) < 0) {
                        shouldTrigger = true;
                        message = String.format("Price Drop Alert: %s at %s dropped to ₹%s (target: ₹%s)",
                                alert.getCommodity().getName(), alert.getMarket().getName(), currentPrice, targetPrice);
                    }
                }
                break;

            case PRICE_INCREASE:
                if (currentPrice.compareTo(targetPrice) >= 0) {
                    if (lastTrig == null || (lastEval != null && lastEval.compareTo(targetPrice) < 0) || currentPrice.compareTo(lastTrig) > 0) {
                        shouldTrigger = true;
                        message = String.format("Price Surge Alert: %s at %s rose to ₹%s (target: ₹%s)",
                                alert.getCommodity().getName(), alert.getMarket().getName(), currentPrice, targetPrice);
                    }
                }
                break;

            case THRESHOLD_CROSS:
                if (lastEval == null) {
                    shouldTrigger = true;
                    message = String.format("Threshold Alert: %s at %s is now ₹%s (threshold: ₹%s)",
                            alert.getCommodity().getName(), alert.getMarket().getName(), currentPrice, targetPrice);
                } else {
                    boolean wasBelow = lastEval.compareTo(targetPrice) < 0;
                    boolean isAboveOrEqual = currentPrice.compareTo(targetPrice) >= 0;
                    boolean wasAbove = lastEval.compareTo(targetPrice) > 0;
                    boolean isBelowOrEqual = currentPrice.compareTo(targetPrice) <= 0;

                    if ((wasBelow && isAboveOrEqual) || (wasAbove && isBelowOrEqual)) {
                        shouldTrigger = true;
                        message = String.format("Threshold Crossing Alert: %s at %s crossed threshold ₹%s (now ₹%s)",
                                alert.getCommodity().getName(), alert.getMarket().getName(), targetPrice, currentPrice);
                    }
                }
                break;
        }

        return new EvaluationResult(shouldTrigger, currentPrice, message);
    }
}
