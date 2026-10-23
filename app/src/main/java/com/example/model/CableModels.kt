package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CableBlack
import com.example.ui.theme.CableBlue
import com.example.ui.theme.CableGreen
import com.example.ui.theme.CableRed
import com.example.ui.theme.CableWhite
import com.example.ui.theme.CableYellow

/**
 * Standard Brazilian electrical cable cross-sections (NBR NM 247-3 / NBR 5410)
 * Flexible copper conductors (750V / 1kV, Classe 5, PVC 70°C).
 */
data class CableSectionInfo(
    val sectionMm2: Double,
    val displayLabel: String,
    val nominalCurrentA: Double,     // Capacidade de condução corrente método B1 (eletroduto embutido, 2 condutores carregados)
    val recommendedBreakerA: Int,     // Disjuntor termomagnético recomendado (curva C)
    val typicalApplication: String,   // Aplicação típica na instalação residencial/predial
    val copperWeightKgKm: Double,     // Peso aproximado de cobre puro em kg por km (1000m)
    val nominalDiameterMm: Double     // Diâmetro externo aproximado
)

object StandardCableSections {
    val ALL: List<CableSectionInfo> = listOf(
        CableSectionInfo(0.5, "0,5 mm²", 9.0, 6, "Sinalização, interfonia e comando", 4.8, 2.1),
        CableSectionInfo(0.75, "0,75 mm²", 12.0, 10, "Luminárias, abajures e ligação de aparelhos", 7.2, 2.3),
        CableSectionInfo(1.0, "1,0 mm²", 14.5, 10, "Circuitos de comando e pequenos pontos", 9.6, 2.5),
        CableSectionInfo(1.5, "1,5 mm²", 17.5, 10, "Mínimo NBR 5410 para Iluminação", 14.4, 2.8),
        CableSectionInfo(2.5, "2,5 mm²", 24.0, 20, "Mínimo NBR 5410 para Tomadas de Uso Geral (TUG)", 24.0, 3.5),
        CableSectionInfo(4.0, "4,0 mm²", 32.0, 25, "Chuveiros 127V/220V (até 5500W), Tomadas TUE", 38.4, 4.1),
        CableSectionInfo(6.0, "6,0 mm²", 41.0, 32, "Chuveiros potentes (7500W), Ar-Condicionado grande", 57.6, 4.7),
        CableSectionInfo(10.0, "10,0 mm²", 57.0, 50, "Entrada padrão, alimentador QDC bifásico/trifásico", 96.0, 6.0),
        CableSectionInfo(16.0, "16,0 mm²", 76.0, 70, "Alimentador principal de residências de alto padrão", 153.6, 7.2),
        CableSectionInfo(25.0, "25,0 mm²", 101.0, 90, "Entradas de força comerciais e painéis centrais", 240.0, 8.8),
        CableSectionInfo(35.0, "35,0 mm²", 125.0, 100, "Entradas gerais de edifícios e subestações", 336.0, 10.2),
        CableSectionInfo(50.0, "50,0 mm²", 151.0, 125, "Alimentadores industriais e comerciais pesados", 480.0, 12.1),
        CableSectionInfo(70.0, "70,0 mm²", 192.0, 175, "Grandes centros de medição e alimentadores primários", 672.0, 14.2),
        CableSectionInfo(95.0, "95,0 mm²", 232.0, 200, "Distribuição pesada e entradas de edifícios", 912.0, 16.5),
        CableSectionInfo(120.0, "120,0 mm²", 269.0, 250, "Alimentação de transformadores e grandes quadros", 1152.0, 18.2)
    )

    fun getBySection(section: Double): CableSectionInfo {
        return ALL.find { it.sectionMm2 == section }
            ?: CableSectionInfo(section, "$section mm²", 0.0, 0, "Uso especializado", 0.0, 0.0)
    }
}

/**
 * Standard Wire Insulation Colors (NBR 5410 standard recommendations)
 */
enum class CableColor(
    val ptName: String,
    val color: Color,
    val textColor: Color,
    val nbrRole: String
) {
    BLUE("Azul Claro", CableBlue, Color.White, "Obrigatório para condutor Neutro (N)"),
    BLACK("Preto", CableBlack, Color.White, "Condutor de Fase (R, S, T)"),
    RED("Vermelho", CableRed, Color.White, "Condutor de Fase (R, S, T)"),
    GREEN("Verde", CableGreen, Color.White, "Obrigatório para condutor de Proteção / Terra (PE)"),
    WHITE("Branco", CableWhite, Color(0xFF1E293B), "Condutor de Fase ou Retorno de interruptores"),
    YELLOW("Amarelo", CableYellow, Color(0xFF1E293B), "Condutor de Fase ou Retorno de iluminação")
}

/**
 * Cable item in a quote / project list
 */
data class BudgetItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sectionMm2: Double,
    val color: CableColor,
    val meters: Double,
    val unitPricePerMeter: Double,
    val rollDiscountPercent: Double = 5.0 // Desconto padrão de 5% em rolos de 100m
) {
    // Quantidade de rolos fechados de 100m e metros avulsos
    val rolls100mCount: Int
        get() = (meters / 100).toInt()

    val leftoverMeters: Double
        get() = meters % 100

    val sectionInfo: CableSectionInfo
        get() = StandardCableSections.getBySection(sectionMm2)

    val unitPricePerRoll100m: Double
        get() = unitPricePerMeter * 100 * (1.0 - rollDiscountPercent / 100.0)

    val totalPrice: Double
        get() {
            val rollsCost = rolls100mCount * unitPricePerRoll100m
            val leftoverCost = leftoverMeters * unitPricePerMeter
            return rollsCost + leftoverCost
        }

    val totalCopperWeightKg: Double
        get() = (meters / 1000.0) * sectionInfo.copperWeightKgKm
}

/**
 * Weekly Supplier Price Table representation
 */
data class SupplierWeeklyTable(
    val id: String,
    val supplierName: String,
    val weekNumber: Int,
    val year: Int,
    val startDate: String,
    val endDate: String,
    val isActive: Boolean = false,
    val copperAdjustmentPercent: Double = 0.0, // Ex: +2.5% ou -1.2%
    val prices: Map<Double, Double> = emptyMap(), // sectionMm2 -> pricePerMeter
    val rollDiscountPercent: Double = 5.0, // 5% discount for full 100m roll
    val updatedAt: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    val weekTitle: String
        get() = "Semana $weekNumber/$year ($startDate a $endDate)"

    fun getPrice(sectionMm2: Double): Double {
        return prices[sectionMm2] ?: 0.0
    }

    fun getRollPrice(sectionMm2: Double): Double {
        val meterPrice = getPrice(sectionMm2)
        return meterPrice * 100 * (1.0 - rollDiscountPercent / 100.0)
    }
}

/**
 * Saved Project / Quote for customer
 */
data class SavedQuote(
    val id: String = java.util.UUID.randomUUID().toString(),
    val clientName: String,
    val projectName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val supplierName: String,
    val weekRef: String,
    val items: List<BudgetItem>,
    val discountPercent: Double = 0.0,
    val marginPercent: Double = 0.0,
    val notes: String = ""
) {
    val subtotal: Double
        get() = items.sumOf { it.totalPrice }

    val totalMeters: Double
        get() = items.sumOf { it.meters }

    val totalCopperWeightKg: Double
        get() = items.sumOf { it.totalCopperWeightKg }

    val finalTotal: Double
        get() {
            val withDiscount = subtotal * (1.0 - discountPercent / 100.0)
            return withDiscount * (1.0 + marginPercent / 100.0)
        }
}
