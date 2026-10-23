package com.example.model

import java.util.Locale
import kotlin.math.sqrt

/**
 * Standard NBR 5410 Voltage Drop & Ampacity Calculations for flexible copper cables
 */
enum class CircuitType(val label: String, val kFactor: Double) {
    SINGLE_OR_TWO_PHASE("Monofásico / Bifásico (2 condutores)", 2.0),
    THREE_PHASE("Trifásico (3 ou 4 condutores)", sqrt(3.0)) // ~1.73205
}

enum class VoltageDropStatus(val title: String, val description: String, val isCompliant: Boolean) {
    COMPLIANT("Em Conformidade NBR 5410", "Queda de tensão e corrente dentro dos limites da NBR 5410.", true),
    WARNING("Atenção (Alimentador)", "Perda entre 4% e 7%. Aceitável apenas para alimentadores dedicados.", true),
    NON_COMPLIANT("Queda Excessiva (Não Conforme)", "Perda acima de 7%. Risco de perda de rendimento de aparelhos.", false),
    OVERCURRENT_HAZARD("Cabo NÃO está de acordo com a corrente!", "Cabo não suporta a corrente informada! Risco de superaquecimento e incêndio conforme NBR 5410.", false)
}

data class VoltageDropResult(
    val voltageDropVolts: Double,
    val voltageDropPercent: Double,
    val finalVoltageAtLoad: Double,
    val status: VoltageDropStatus,
    val suggestedSectionMm2: Double,
    val totalPowerLossWatts: Double,
    val cableMaxAmpacityA: Double,
    val isAmpacityExceeded: Boolean,
    val ampacityOverloadPercent: Double,
    val minSectionForCurrentMm2: Double,
    val ampacityWarningMessage: String?
)

object VoltageDropCalculator {
    // Resistividade do cobre eletrolítico em regime normal a 70°C conforme NBR 5410 (ohm * mm² / m)
    const val COPPER_RESISTIVITY_70C = 0.0210
    // Resistividade a 20°C
    const val COPPER_RESISTIVITY_20C = 0.0178

    fun calculate(
        distanceMeters: Double,
        sectionMm2: Double,
        currentAmperes: Double,
        nominalVoltageVolts: Double,
        circuitType: CircuitType,
        useOperatingTemp70C: Boolean = true
    ): VoltageDropResult {
        val safeSection = if (sectionMm2 <= 0) 2.5 else sectionMm2
        val safeVoltage = if (nominalVoltageVolts <= 0) 220.0 else nominalVoltageVolts
        val safeDistance = distanceMeters.coerceAtLeast(0.0)
        val safeCurrent = currentAmperes.coerceAtLeast(0.0)

        val selectedSectionInfo = StandardCableSections.getBySection(safeSection)
        val cableMaxAmpacity = selectedSectionInfo.nominalCurrentA
        val isAmpacityExceeded = safeCurrent > cableMaxAmpacity && safeCurrent > 0

        // Bitola mínima necessária apenas para a corrente (critério térmico de capacidade de condução NBR 5410 Método B1)
        val minSectionForCurrent = StandardCableSections.ALL.firstOrNull { it.nominalCurrentA >= safeCurrent }?.sectionMm2
            ?: StandardCableSections.ALL.last().sectionMm2

        val rho = if (useOperatingTemp70C) COPPER_RESISTIVITY_70C else COPPER_RESISTIVITY_20C
        val k = circuitType.kFactor

        // Delta V = (k * rho * L * I) / S
        val deltaV = if (safeDistance > 0 && safeCurrent > 0) {
            (k * rho * safeDistance * safeCurrent) / safeSection
        } else {
            0.0
        }
        val deltaVPercent = if (safeVoltage > 0) (deltaV / safeVoltage) * 100.0 else 0.0
        val finalVoltage = (safeVoltage - deltaV).coerceAtLeast(0.0)

        // Resistance R = rho * L / S (per wire)
        // Power loss P_loss = k * R * I^2
        val resistancePerWire = if (safeDistance > 0) (rho * safeDistance) / safeSection else 0.0
        val powerLoss = when (circuitType) {
            CircuitType.SINGLE_OR_TWO_PHASE -> 2.0 * resistancePerWire * safeCurrent * safeCurrent
            CircuitType.THREE_PHASE -> 3.0 * resistancePerWire * safeCurrent * safeCurrent
        }

        // Bitola necessária para queda de tensão <= 4%
        val minSectionForVd = if (safeDistance > 0 && safeCurrent > 0) {
            (k * rho * safeDistance * safeCurrent) / (0.04 * safeVoltage)
        } else {
            safeSection
        }
        val suggestedSectionForVd = StandardCableSections.ALL.firstOrNull { it.sectionMm2 >= minSectionForVd }?.sectionMm2
            ?: StandardCableSections.ALL.last().sectionMm2

        // A bitola sugerida FINAL é o MAIOR entre o critério de queda de tensão e o critério de capacidade de corrente
        val finalSuggestedSection = maxOf(suggestedSectionForVd, minSectionForCurrent)

        val overloadPercent = if (isAmpacityExceeded && cableMaxAmpacity > 0) {
            ((safeCurrent - cableMaxAmpacity) / cableMaxAmpacity) * 100.0
        } else {
            0.0
        }

        val status = when {
            isAmpacityExceeded -> VoltageDropStatus.OVERCURRENT_HAZARD
            deltaVPercent <= 4.0 -> VoltageDropStatus.COMPLIANT
            deltaVPercent <= 7.0 -> VoltageDropStatus.WARNING
            else -> VoltageDropStatus.NON_COMPLIANT
        }

        val ampacityWarning = if (isAmpacityExceeded) {
            "O cabo de ${safeSection.toString().replace('.', ',')} mm² NÃO está de acordo com a corrente de ${"%.1f".format(Locale.US, safeCurrent).replace('.', ',')}A! Este condutor suporta no máximo ${cableMaxAmpacity.toInt()}A (NBR 5410). A corrente aplicada ultrapassa a capacidade em +${overloadPercent.toInt()}%, gerando risco grave de superaquecimento e incêndio! A bitola mínima obrigatória para esta corrente é de ${minSectionForCurrent.toString().replace('.', ',')} mm²."
        } else {
            null
        }

        return VoltageDropResult(
            voltageDropVolts = deltaV,
            voltageDropPercent = deltaVPercent,
            finalVoltageAtLoad = finalVoltage,
            status = status,
            suggestedSectionMm2 = finalSuggestedSection,
            totalPowerLossWatts = powerLoss,
            cableMaxAmpacityA = cableMaxAmpacity,
            isAmpacityExceeded = isAmpacityExceeded,
            ampacityOverloadPercent = overloadPercent,
            minSectionForCurrentMm2 = minSectionForCurrent,
            ampacityWarningMessage = ampacityWarning
        )
    }
}

/**
 * Power & Ohm's Law Calculator (P = V * I * FP)
 */
enum class PowerCalcMode(val title: String) {
    CALCULATE_CURRENT("Calcular Ampères (I)"),
    CALCULATE_POWER("Calcular Potência (P)"),
    CALCULATE_VOLTAGE("Calcular Tensão (V)"),
    SIZING_BY_AMPERES("Ampères ➔ Cabo Mínimo")
}

data class PowerCalcResult(
    val calculatedValue: Double,
    val unit: String,
    val formatted: String,
    val formulaUsed: String
)

object PowerCalculator {
    fun calculateCurrent(
        powerWatts: Double,
        voltageVolts: Double,
        powerFactor: Double = 1.0,
        isThreePhase: Boolean = false
    ): PowerCalcResult {
        val factor = if (isThreePhase) sqrt(3.0) else 1.0
        val divisor = voltageVolts * factor * powerFactor.coerceIn(0.1, 1.0)
        val current = if (divisor > 0) powerWatts / divisor else 0.0

        val formula = if (isThreePhase) "I = P / (√3 × V × FP)" else "I = P / (V × FP)"
        return PowerCalcResult(
            calculatedValue = current,
            unit = "A",
            formatted = "%.2f A".format(Locale.US, current).replace('.', ','),
            formulaUsed = formula
        )
    }

    fun calculatePower(
        voltageVolts: Double,
        currentAmperes: Double,
        powerFactor: Double = 1.0,
        isThreePhase: Boolean = false
    ): PowerCalcResult {
        val factor = if (isThreePhase) sqrt(3.0) else 1.0
        val powerWatts = voltageVolts * currentAmperes * factor * powerFactor.coerceIn(0.1, 1.0)

        val formula = if (isThreePhase) "P = √3 × V × I × FP" else "P = V × I × FP"
        val formatted = if (powerWatts >= 1000) {
            "%.2f kW (%.0f W)".format(Locale.US, powerWatts / 1000.0, powerWatts).replace('.', ',')
        } else {
            "%.1f W".format(Locale.US, powerWatts).replace('.', ',')
        }

        return PowerCalcResult(
            calculatedValue = powerWatts,
            unit = "W",
            formatted = formatted,
            formulaUsed = formula
        )
    }

    fun calculateVoltage(
        powerWatts: Double,
        currentAmperes: Double,
        powerFactor: Double = 1.0,
        isThreePhase: Boolean = false
    ): PowerCalcResult {
        val factor = if (isThreePhase) sqrt(3.0) else 1.0
        val divisor = currentAmperes * factor * powerFactor.coerceIn(0.1, 1.0)
        val voltage = if (divisor > 0) powerWatts / divisor else 0.0

        val formula = if (isThreePhase) "V = P / (√3 × I × FP)" else "V = P / (I × FP)"
        return PowerCalcResult(
            calculatedValue = voltage,
            unit = "V",
            formatted = "%.1f V".format(Locale.US, voltage).replace('.', ','),
            formulaUsed = formula
        )
    }

    /**
     * Recomenda o cabo mínimo com base estritamente na capacidade de condução de corrente (Ampères)
     * Método B1 (condutores isolados em eletroduto embutido em alvenaria - NBR 5410 Tabela 36)
     */
    fun findMinCableForCurrent(currentAmperes: Double): CableSectionInfo {
        if (currentAmperes <= 0) return StandardCableSections.ALL.first()
        return StandardCableSections.ALL.firstOrNull { it.nominalCurrentA >= currentAmperes }
            ?: StandardCableSections.ALL.last()
    }
}
