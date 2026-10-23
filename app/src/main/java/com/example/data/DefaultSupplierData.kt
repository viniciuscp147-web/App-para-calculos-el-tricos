package com.example.data

import com.example.model.BudgetItem
import com.example.model.CableColor
import com.example.model.SavedQuote
import com.example.model.SupplierWeeklyTable
import org.json.JSONArray
import org.json.JSONObject

object JsonHelper {
    fun pricesToJson(prices: Map<Double, Double>): String {
        val json = JSONObject()
        prices.forEach { (section, price) ->
            json.put(section.toString(), price)
        }
        return json.toString()
    }

    fun jsonToPrices(jsonStr: String): Map<Double, Double> {
        val map = mutableMapOf<Double, Double>()
        if (jsonStr.isBlank()) return map
        try {
            val json = JSONObject(jsonStr)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val section = key.toDoubleOrNull() ?: continue
                val price = json.optDouble(key, 0.0)
                map[section] = price
            }
        } catch (_: Exception) {
            // fallback
        }
        return map
    }

    fun itemsToJson(items: List<BudgetItem>): String {
        val arr = JSONArray()
        items.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("sectionMm2", item.sectionMm2)
            obj.put("color", item.color.name)
            obj.put("meters", item.meters)
            obj.put("unitPricePerMeter", item.unitPricePerMeter)
            obj.put("rollDiscountPercent", item.rollDiscountPercent)
            arr.put(obj)
        }
        return arr.toString()
    }

    fun jsonToItems(jsonStr: String): List<BudgetItem> {
        val list = mutableListOf<BudgetItem>()
        if (jsonStr.isBlank()) return list
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val colorName = obj.optString("color", CableColor.BLACK.name)
                val color = try {
                    CableColor.valueOf(colorName)
                } catch (_: Exception) {
                    CableColor.BLACK
                }
                list.add(
                    BudgetItem(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        sectionMm2 = obj.optDouble("sectionMm2", 2.5),
                        color = color,
                        meters = obj.optDouble("meters", 100.0),
                        unitPricePerMeter = obj.optDouble("unitPricePerMeter", 4.60),
                        rollDiscountPercent = obj.optDouble("rollDiscountPercent", 5.0)
                    )
                )
            }
        } catch (_: Exception) {
            // fallback
        }
        return list
    }
}

object DefaultSupplierData {

    // Realistic baseline prices per meter in BRL (R$)
    val silPrices: Map<Double, Double> = mapOf(
        0.5 to 1.45,
        0.75 to 1.85,
        1.0 to 2.20,
        1.5 to 2.85,
        2.5 to 4.55,
        4.0 to 7.30,
        6.0 to 11.10,
        10.0 to 18.80,
        16.0 to 29.50,
        25.0 to 45.90,
        35.0 to 63.50,
        50.0 to 91.20,
        70.0 to 130.50,
        95.0 to 179.00,
        120.0 to 228.00
    )

    val cobrecomPrices: Map<Double, Double> = mapOf(
        0.5 to 1.42,
        0.75 to 1.80,
        1.0 to 2.15,
        1.5 to 2.78,
        2.5 to 4.48,
        4.0 to 7.15,
        6.0 to 10.90,
        10.0 to 18.40,
        16.0 to 28.90,
        25.0 to 45.20,
        35.0 to 62.10,
        50.0 to 89.80,
        70.0 to 128.00,
        95.0 to 175.00,
        120.0 to 224.00
    )

    val prysmianPrices: Map<Double, Double> = mapOf(
        0.5 to 1.55,
        0.75 to 1.95,
        1.0 to 2.35,
        1.5 to 3.05,
        2.5 to 4.85,
        4.0 to 7.70,
        6.0 to 11.65,
        10.0 to 19.60,
        16.0 to 30.80,
        25.0 to 47.90,
        35.0 to 66.00,
        50.0 to 94.50,
        70.0 to 135.00,
        95.0 to 185.00,
        120.0 to 235.00
    )

    fun getDefaultEntities(): List<PriceTableEntity> {
        val currentMillis = System.currentTimeMillis()

        val silActive = PriceTableEntity(
            id = "table_sil_w43_2026",
            supplierName = "Sil Fios e Cabos",
            weekNumber = 43,
            year = 2026,
            startDate = "19/10/2026",
            endDate = "25/10/2026",
            isActive = true,
            copperAdjustmentPercent = 2.4,
            pricesJson = JsonHelper.pricesToJson(silPrices),
            rollDiscountPercent = 5.0,
            updatedAt = currentMillis,
            notes = "Tabela oficial semanal atualizada com base na cotação LME do cobre (+2,4%)."
        )

        val cobrecom = PriceTableEntity(
            id = "table_cobrecom_w43_2026",
            supplierName = "Cobrecom Cabos Elétricos",
            weekNumber = 43,
            year = 2026,
            startDate = "19/10/2026",
            endDate = "25/10/2026",
            isActive = false,
            copperAdjustmentPercent = 1.8,
            pricesJson = JsonHelper.pricesToJson(cobrecomPrices),
            rollDiscountPercent = 6.0,
            updatedAt = currentMillis - 86400000,
            notes = "Tabela distribuidor Cobrecom vigente para pronta entrega."
        )

        val prysmian = PriceTableEntity(
            id = "table_prysmian_w43_2026",
            supplierName = "Prysmian Group (Superastic)",
            weekNumber = 43,
            year = 2026,
            startDate = "19/10/2026",
            endDate = "25/10/2026",
            isActive = false,
            copperAdjustmentPercent = 3.0,
            pricesJson = JsonHelper.pricesToJson(prysmianPrices),
            rollDiscountPercent = 4.0,
            updatedAt = currentMillis - 172800000,
            notes = "Linha antichama BWF com certificação Inmetro e selo Qualifio."
        )

        // Previous week for historical reference and price trend comparison
        val silPrevious = PriceTableEntity(
            id = "table_sil_w42_2026",
            supplierName = "Sil Fios e Cabos",
            weekNumber = 42,
            year = 2026,
            startDate = "12/10/2026",
            endDate = "18/10/2026",
            isActive = false,
            copperAdjustmentPercent = 0.0,
            pricesJson = JsonHelper.pricesToJson(
                silPrices.mapValues { (_, price) ->
                    // 2.4% cheaper in previous week
                    (price / 1.024 * 100.0).toLong() / 100.0
                }
            ),
            rollDiscountPercent = 5.0,
            updatedAt = currentMillis - 7 * 86400000,
            notes = "Tabela arquivada da semana anterior."
        )

        return listOf(silActive, cobrecom, prysmian, silPrevious)
    }

    fun PriceTableEntity.toDomain(): SupplierWeeklyTable {
        return SupplierWeeklyTable(
            id = id,
            supplierName = supplierName,
            weekNumber = weekNumber,
            year = year,
            startDate = startDate,
            endDate = endDate,
            isActive = isActive,
            copperAdjustmentPercent = copperAdjustmentPercent,
            prices = JsonHelper.jsonToPrices(pricesJson),
            rollDiscountPercent = rollDiscountPercent,
            updatedAt = updatedAt,
            notes = notes
        )
    }

    fun SupplierWeeklyTable.toEntity(): PriceTableEntity {
        return PriceTableEntity(
            id = id,
            supplierName = supplierName,
            weekNumber = weekNumber,
            year = year,
            startDate = startDate,
            endDate = endDate,
            isActive = isActive,
            copperAdjustmentPercent = copperAdjustmentPercent,
            pricesJson = JsonHelper.pricesToJson(prices),
            rollDiscountPercent = rollDiscountPercent,
            updatedAt = updatedAt,
            notes = notes
        )
    }

    fun SavedQuoteEntity.toDomain(): SavedQuote {
        return SavedQuote(
            id = id,
            clientName = clientName,
            projectName = projectName,
            createdAt = createdAt,
            supplierName = supplierName,
            weekRef = weekRef,
            items = JsonHelper.jsonToItems(itemsJson),
            discountPercent = discountPercent,
            marginPercent = marginPercent,
            notes = notes
        )
    }

    fun SavedQuote.toEntity(): SavedQuoteEntity {
        return SavedQuoteEntity(
            id = id,
            clientName = clientName,
            projectName = projectName,
            createdAt = createdAt,
            supplierName = supplierName,
            weekRef = weekRef,
            itemsJson = JsonHelper.itemsToJson(items),
            discountPercent = discountPercent,
            marginPercent = marginPercent,
            notes = notes
        )
    }
}
