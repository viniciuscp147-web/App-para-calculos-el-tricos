package com.example

import com.example.model.BudgetItem
import com.example.model.CableColor
import com.example.model.CircuitType
import com.example.model.PowerCalculator
import com.example.model.StandardCableSections
import com.example.model.VoltageDropCalculator
import com.example.model.VoltageDropStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class ExampleUnitTest {

    @Test
    fun testVoltageDropSinglePhase() {
        // Distance 30m, 2.5 mm², 16A, 220V nominal, single phase (k = 2)
        // Delta V = (2 * 0.0210 * 30 * 16) / 2.5 = 20.16 / 2.5 = 8.064 V
        // Delta V% = (8.064 / 220) * 100 = 3.665% (Compliant <= 4%)
        val result = VoltageDropCalculator.calculate(
            distanceMeters = 30.0,
            sectionMm2 = 2.5,
            currentAmperes = 16.0,
            nominalVoltageVolts = 220.0,
            circuitType = CircuitType.SINGLE_OR_TWO_PHASE
        )

        assertEquals(8.064, result.voltageDropVolts, 0.05)
        assertEquals(3.665, result.voltageDropPercent, 0.05)
        assertEquals(VoltageDropStatus.COMPLIANT, result.status)
        assertTrue(result.status.isCompliant)
    }

    @Test
    fun testVoltageDropThreePhase() {
        // Distance 50m, 4.0 mm², 25A, 380V nominal, three phase (k = sqrt(3) ~ 1.732)
        // Delta V = (1.73205 * 0.0210 * 50 * 25) / 4.0 = 45.466 / 4.0 = 11.366 V
        // Delta V% = (11.366 / 380) * 100 = 2.99% (Compliant <= 4%)
        val result = VoltageDropCalculator.calculate(
            distanceMeters = 50.0,
            sectionMm2 = 4.0,
            currentAmperes = 25.0,
            nominalVoltageVolts = 380.0,
            circuitType = CircuitType.THREE_PHASE
        )

        assertEquals(11.366, result.voltageDropVolts, 0.1)
        assertEquals(2.99, result.voltageDropPercent, 0.1)
        assertEquals(VoltageDropStatus.COMPLIANT, result.status)
    }

    @Test
    fun testPowerCalculatorAmperesAndWatts() {
        // Calculate Current: 5500W at 220V, FP = 1.0 -> I = 5500 / 220 = 25.0 A
        val currentResult = PowerCalculator.calculateCurrent(5500.0, 220.0, 1.0, false)
        assertEquals(25.0, currentResult.calculatedValue, 0.01)

        // Calculate Power: 220V at 16A, FP = 1.0 -> P = 3520 W
        val powerResult = PowerCalculator.calculatePower(220.0, 16.0, 1.0, false)
        assertEquals(3520.0, powerResult.calculatedValue, 0.01)

        // Calculate Voltage: 4400W at 20A, FP = 1.0 -> V = 220 V
        val voltageResult = PowerCalculator.calculateVoltage(4400.0, 20.0, 1.0, false)
        assertEquals(220.0, voltageResult.calculatedValue, 0.01)
    }

    @Test
    fun testCableCalculationsForRollsAndCut() {
        val item = BudgetItem(
            sectionMm2 = 2.5,
            color = CableColor.BLUE,
            meters = 250.0,
            unitPricePerMeter = 4.55,
            rollDiscountPercent = 5.0
        )

        assertEquals(2, item.rolls100mCount)
        assertEquals(50.0, item.leftoverMeters, 0.001)
        val expectedRollPrice = 432.25
        assertEquals(expectedRollPrice, item.unitPricePerRoll100m, 0.01)
        val expectedTotal = (2 * expectedRollPrice) + (50 * 4.55)
        assertEquals(expectedTotal, item.totalPrice, 0.01)
    }

    @Test
    fun testVoltageDropAmpacityOverloadHazard() {
        // Distance 10m, 2.5 mm² cable, 40A, 220V
        // Delta V = (2 * 0.0210 * 10 * 40) / 2.5 = 6.72 V
        // Delta V% = (6.72 / 220) * 100 = 3.05% (Within 4% voltage drop!)
        // BUT 2.5 mm² only supports 24A (NBR 5410 B1).
        // 40A is a 66.7% overload -> MUST trigger OVERCURRENT_HAZARD!
        val result = VoltageDropCalculator.calculate(
            distanceMeters = 10.0,
            sectionMm2 = 2.5,
            currentAmperes = 40.0,
            nominalVoltageVolts = 220.0,
            circuitType = CircuitType.SINGLE_OR_TWO_PHASE
        )

        assertTrue(result.voltageDropPercent < 4.0)
        assertTrue(result.isAmpacityExceeded)
        assertEquals(VoltageDropStatus.OVERCURRENT_HAZARD, result.status)
        assertFalse(result.status.isCompliant)
        assertEquals(24.0, result.cableMaxAmpacityA, 0.1)
        assertEquals(6.0, result.minSectionForCurrentMm2, 0.01)
        assertEquals(6.0, result.suggestedSectionMm2, 0.01)
        assertEquals("Cabo NÃO está de acordo com a corrente!", result.status.title)
        assertTrue(result.ampacityWarningMessage != null)
    }

    @Test
    fun testVoltageDropWithZeroDistanceStillFlagsOverload() {
        // Distance 0m, 2.5 mm² cable, 40A, 220V
        // Thermal ampacity is independent of distance: 40A on 2.5 mm² is always dangerous!
        val result = VoltageDropCalculator.calculate(
            distanceMeters = 0.0,
            sectionMm2 = 2.5,
            currentAmperes = 40.0,
            nominalVoltageVolts = 220.0,
            circuitType = CircuitType.SINGLE_OR_TWO_PHASE
        )

        assertTrue(result.isAmpacityExceeded)
        assertEquals(VoltageDropStatus.OVERCURRENT_HAZARD, result.status)
        assertEquals(6.0, result.minSectionForCurrentMm2, 0.01)
    }

    @Test
    fun testPowerCalculatorSizingByAmperes() {
        // 15A -> 1.5 mm² (17.5A)
        val cable15A = PowerCalculator.findMinCableForCurrent(15.0)
        assertEquals(1.5, cable15A.sectionMm2, 0.01)

        // 20A -> 2.5 mm² (24A)
        val cable20A = PowerCalculator.findMinCableForCurrent(20.0)
        assertEquals(2.5, cable20A.sectionMm2, 0.01)

        // 30A -> 4.0 mm² (32A)
        val cable30A = PowerCalculator.findMinCableForCurrent(30.0)
        assertEquals(4.0, cable30A.sectionMm2, 0.01)

        // 40A -> 6.0 mm² (41A)
        val cable40A = PowerCalculator.findMinCableForCurrent(40.0)
        assertEquals(6.0, cable40A.sectionMm2, 0.01)

        // 55A -> 10.0 mm² (57A)
        val cable55A = PowerCalculator.findMinCableForCurrent(55.0)
        assertEquals(10.0, cable55A.sectionMm2, 0.01)
    }
}

