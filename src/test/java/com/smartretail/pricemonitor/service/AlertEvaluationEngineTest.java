package com.smartretail.pricemonitor.service;

import com.smartretail.pricemonitor.constants.AlertType;
import com.smartretail.pricemonitor.entity.Alert;
import com.smartretail.pricemonitor.entity.Commodity;
import com.smartretail.pricemonitor.entity.Market;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AlertEvaluationEngineTest {

    private Commodity tomatoKg;
    private Market azadpurMarket;

    @BeforeEach
    void setUp() {
        tomatoKg = Commodity.builder()
                .id(1L)
                .name("Tomato (Hybrid)")
                .unit("kg")
                .baseBenchmarkPrice(BigDecimal.valueOf(40.00))
                .build();

        azadpurMarket = Market.builder()
                .id(1L)
                .name("Azadpur Mandi")
                .city("Delhi")
                .state("Delhi")
                .build();
    }

    @Test
    @DisplayName("1. Price Decrease Alert - Initial trigger when current price <= target price")
    void testPriceDecrease_InitialTrigger() {
        Alert alert = Alert.builder()
                .id(101L)
                .commodity(tomatoKg)
                .market(azadpurMarket)
                .alertType(AlertType.PRICE_DECREASE)
                .targetPrice(BigDecimal.valueOf(35.00))
                .isActive(true)
                .build();

        AlertEvaluationEngine.EvaluationResult res = AlertEvaluationEngine.evaluate(alert, BigDecimal.valueOf(32.00), "kg");
        assertTrue(res.isTriggered(), "Should trigger when initial price 32 <= target 35");
        assertEquals(BigDecimal.valueOf(32.00), res.getNormalizedPrice());
    }

    @Test
    @DisplayName("2. Price Decrease Alert - Deduplication on repeated scheduler run with same price")
    void testPriceDecrease_Deduplication() {
        Alert alert = Alert.builder()
                .id(101L)
                .commodity(tomatoKg)
                .market(azadpurMarket)
                .alertType(AlertType.PRICE_DECREASE)
                .targetPrice(BigDecimal.valueOf(35.00))
                .lastEvaluatedPrice(BigDecimal.valueOf(32.00))
                .lastTriggeredPrice(BigDecimal.valueOf(32.00))
                .isActive(true)
                .build();

        AlertEvaluationEngine.EvaluationResult res = AlertEvaluationEngine.evaluate(alert, BigDecimal.valueOf(32.00), "kg");
        assertFalse(res.isTriggered(), "Repeated scheduler run with same price 32 should NOT re-trigger duplicate notification");
    }

    @Test
    @DisplayName("3. Price Decrease Alert - Re-trigger when price drops further")
    void testPriceDecrease_RetriggerOnFurtherDrop() {
        Alert alert = Alert.builder()
                .id(101L)
                .commodity(tomatoKg)
                .market(azadpurMarket)
                .alertType(AlertType.PRICE_DECREASE)
                .targetPrice(BigDecimal.valueOf(35.00))
                .lastEvaluatedPrice(BigDecimal.valueOf(32.00))
                .lastTriggeredPrice(BigDecimal.valueOf(32.00))
                .isActive(true)
                .build();

        AlertEvaluationEngine.EvaluationResult res = AlertEvaluationEngine.evaluate(alert, BigDecimal.valueOf(28.00), "kg");
        assertTrue(res.isTriggered(), "Should re-trigger when price drops further from 32 to 28");
    }

    @Test
    @DisplayName("4. Price Increase Alert - Initial trigger and deduplication")
    void testPriceIncrease_Semantics() {
        Alert alert = Alert.builder()
                .id(102L)
                .commodity(tomatoKg)
                .market(azadpurMarket)
                .alertType(AlertType.PRICE_INCREASE)
                .targetPrice(BigDecimal.valueOf(50.00))
                .isActive(true)
                .build();

        // 1st run: 55 >= 50 -> Triggers
        AlertEvaluationEngine.EvaluationResult res1 = AlertEvaluationEngine.evaluate(alert, BigDecimal.valueOf(55.00), "kg");
        assertTrue(res1.isTriggered());

        // Update alert state after 1st run
        alert.setLastEvaluatedPrice(BigDecimal.valueOf(55.00));
        alert.setLastTriggeredPrice(BigDecimal.valueOf(55.00));

        // 2nd run: 55 >= 50 -> Deduplicated
        AlertEvaluationEngine.EvaluationResult res2 = AlertEvaluationEngine.evaluate(alert, BigDecimal.valueOf(55.00), "kg");
        assertFalse(res2.isTriggered(), "Same price 55 should be deduplicated");

        // 3rd run: 60 >= 50 -> Triggers further surge
        AlertEvaluationEngine.EvaluationResult res3 = AlertEvaluationEngine.evaluate(alert, BigDecimal.valueOf(60.00), "kg");
        assertTrue(res3.isTriggered(), "Further price surge to 60 should re-trigger alert");
    }

    @Test
    @DisplayName("5. Threshold Crossing Alert - Upper and Lower crossing dynamics")
    void testThresholdCross_Dynamics() {
        Alert alert = Alert.builder()
                .id(103L)
                .commodity(tomatoKg)
                .market(azadpurMarket)
                .alertType(AlertType.THRESHOLD_CROSS)
                .targetPrice(BigDecimal.valueOf(40.00))
                .isActive(true)
                .build();

        // Initial run
        AlertEvaluationEngine.EvaluationResult res1 = AlertEvaluationEngine.evaluate(alert, BigDecimal.valueOf(38.00), "kg");
        assertTrue(res1.isTriggered());

        alert.setLastEvaluatedPrice(BigDecimal.valueOf(38.00));

        // Same side (stay below threshold 40)
        AlertEvaluationEngine.EvaluationResult res2 = AlertEvaluationEngine.evaluate(alert, BigDecimal.valueOf(39.00), "kg");
        assertFalse(res2.isTriggered(), "Staying below threshold 40 should NOT trigger crossing alert");

        // Crossing upwards (from 39 to 42 across 40)
        alert.setLastEvaluatedPrice(BigDecimal.valueOf(39.00));
        AlertEvaluationEngine.EvaluationResult res3 = AlertEvaluationEngine.evaluate(alert, BigDecimal.valueOf(42.00), "kg");
        assertTrue(res3.isTriggered(), "Crossing upwards across threshold 40 should trigger alert");

        // Crossing downwards (from 42 to 35 across 40)
        alert.setLastEvaluatedPrice(BigDecimal.valueOf(42.00));
        AlertEvaluationEngine.EvaluationResult res4 = AlertEvaluationEngine.evaluate(alert, BigDecimal.valueOf(35.00), "kg");
        assertTrue(res4.isTriggered(), "Crossing downwards across threshold 40 should trigger alert");
    }

    @Test
    @DisplayName("6. Unit Conversion - Quintal to kg conversion handling")
    void testUnitConversion_QuintalToKg() {
        Alert alert = Alert.builder()
                .id(104L)
                .commodity(tomatoKg) // Unit: kg
                .market(azadpurMarket)
                .alertType(AlertType.PRICE_DECREASE)
                .targetPrice(BigDecimal.valueOf(35.00))
                .isActive(true)
                .build();

        // 3200 Rs per Quintal (100 kg) = 32 Rs per kg
        AlertEvaluationEngine.EvaluationResult res = AlertEvaluationEngine.evaluate(alert, BigDecimal.valueOf(3200.00), "Quintal");
        assertTrue(res.isTriggered());
        assertEquals(BigDecimal.valueOf(32.00), res.getNormalizedPrice());
    }

    @Test
    @DisplayName("7. Incompatible Units - Reject comparison safely")
    void testIncompatibleUnits() {
        Alert alert = Alert.builder()
                .id(105L)
                .commodity(tomatoKg) // Unit: kg
                .market(azadpurMarket)
                .alertType(AlertType.PRICE_DECREASE)
                .targetPrice(BigDecimal.valueOf(35.00))
                .isActive(true)
                .build();

        // Observed unit Liter vs Commodity unit kg
        AlertEvaluationEngine.EvaluationResult res = AlertEvaluationEngine.evaluate(alert, BigDecimal.valueOf(30.00), "Liter");
        assertFalse(res.isTriggered(), "Incompatible unit comparison (Liter vs kg) must not trigger an alert");
        assertNull(res.getNormalizedPrice());
    }

    @Test
    @DisplayName("8. Disabled Alert - Inactive alert must not trigger")
    void testDisabledAlert() {
        Alert alert = Alert.builder()
                .id(106L)
                .commodity(tomatoKg)
                .market(azadpurMarket)
                .alertType(AlertType.PRICE_DECREASE)
                .targetPrice(BigDecimal.valueOf(50.00))
                .isActive(false) // Inactive
                .build();

        AlertEvaluationEngine.EvaluationResult res = AlertEvaluationEngine.evaluate(alert, BigDecimal.valueOf(20.00), "kg");
        assertFalse(res.isTriggered(), "Disabled alert must not trigger");
    }

    @Test
    @DisplayName("9. Existing Alert Migration - Legacy row with null trigger state evaluates safely")
    void testLegacyNullFieldsMigration() {
        Alert legacyAlert = Alert.builder()
                .id(107L)
                .commodity(tomatoKg)
                .market(azadpurMarket)
                .alertType(AlertType.PRICE_INCREASE)
                .targetPrice(BigDecimal.valueOf(45.00))
                .lastEvaluatedPrice(null)
                .lastEvaluatedAt(null)
                .lastTriggeredPrice(null)
                .lastTriggeredAt(null)
                .isActive(true)
                .build();

        AlertEvaluationEngine.EvaluationResult res = AlertEvaluationEngine.evaluate(legacyAlert, BigDecimal.valueOf(50.00), "kg");
        assertTrue(res.isTriggered(), "Legacy alert with null trigger state must evaluate safely without NPE");
    }
}
