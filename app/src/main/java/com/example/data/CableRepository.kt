package com.example.data

import com.example.data.DefaultSupplierData.toDomain
import com.example.data.DefaultSupplierData.toEntity
import com.example.model.BudgetItem
import com.example.model.SavedQuote
import com.example.model.StandardCableSections
import com.example.model.SupplierWeeklyTable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class CableRepository(
    private val priceTableDao: PriceTableDao,
    private val savedQuoteDao: SavedQuoteDao
) {
    val allTables: Flow<List<SupplierWeeklyTable>> = priceTableDao.getAllTables().map { list ->
        list.map { it.toDomain() }
    }

    val activeTable: Flow<SupplierWeeklyTable?> = priceTableDao.getActiveTableFlow().map { entity ->
        entity?.toDomain()
    }

    val savedQuotes: Flow<List<SavedQuote>> = savedQuoteDao.getAllQuotes().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun checkAndSeedDefaults() {
        if (priceTableDao.getCount() == 0) {
            priceTableDao.insertTables(DefaultSupplierData.getDefaultEntities())
        }
    }

    suspend fun setActiveTable(tableId: String) {
        priceTableDao.setActiveTable(tableId)
    }

    suspend fun updateTable(table: SupplierWeeklyTable) {
        priceTableDao.updateTable(table.toEntity())
    }

    suspend fun updateSectionPrice(tableId: String, sectionMm2: Double, newPrice: Double) {
        val entity = priceTableDao.getTableById(tableId) ?: return
        val currentPrices = JsonHelper.jsonToPrices(entity.pricesJson).toMutableMap()
        currentPrices[sectionMm2] = newPrice
        val updated = entity.copy(
            pricesJson = JsonHelper.pricesToJson(currentPrices),
            updatedAt = System.currentTimeMillis()
        )
        priceTableDao.updateTable(updated)
    }

    suspend fun applyCopperAdjustment(tableId: String, percentage: Double) {
        val entity = priceTableDao.getTableById(tableId) ?: return
        val currentPrices = JsonHelper.jsonToPrices(entity.pricesJson)
        val factor = 1.0 + (percentage / 100.0)
        val newPrices = currentPrices.mapValues { (_, price) ->
            (price * factor * 100.0).toLong() / 100.0
        }
        val updated = entity.copy(
            copperAdjustmentPercent = percentage,
            pricesJson = JsonHelper.pricesToJson(newPrices),
            updatedAt = System.currentTimeMillis()
        )
        priceTableDao.updateTable(updated)
    }

    suspend fun createNewWeeklyTable(
        baseTableId: String?,
        supplierName: String,
        weekNumber: Int,
        year: Int,
        startDate: String,
        endDate: String,
        copperAdjustmentPercent: Double,
        rollDiscountPercent: Double,
        notes: String
    ): String {
        val newId = "table_${supplierName.lowercase().replace(" ", "_")}_w${weekNumber}_$year"

        val basePrices = if (!baseTableId.isNullOrEmpty()) {
            val base = priceTableDao.getTableById(baseTableId)
            if (base != null) JsonHelper.jsonToPrices(base.pricesJson) else DefaultSupplierData.silPrices
        } else {
            DefaultSupplierData.silPrices
        }

        val factor = 1.0 + (copperAdjustmentPercent / 100.0)
        val adjustedPrices = basePrices.mapValues { (_, price) ->
            (price * factor * 100.0).toLong() / 100.0
        }

        val entity = PriceTableEntity(
            id = newId,
            supplierName = supplierName,
            weekNumber = weekNumber,
            year = year,
            startDate = startDate,
            endDate = endDate,
            isActive = true, // Set as current active table
            copperAdjustmentPercent = copperAdjustmentPercent,
            pricesJson = JsonHelper.pricesToJson(adjustedPrices),
            rollDiscountPercent = rollDiscountPercent,
            updatedAt = System.currentTimeMillis(),
            notes = notes
        )

        priceTableDao.clearActiveFlag()
        priceTableDao.insertTable(entity)
        return newId
    }

    suspend fun deleteTable(id: String) {
        priceTableDao.deleteTable(id)
        // If no active table remains, mark the first available table as active
        val currentActive = priceTableDao.getActiveTable()
        if (currentActive == null) {
            val first = priceTableDao.getAllTables()
            // We can handle fallback in ViewModel if needed
        }
    }

    suspend fun resetToDefaultTables() {
        priceTableDao.clearActiveFlag()
        priceTableDao.insertTables(DefaultSupplierData.getDefaultEntities())
        priceTableDao.setActiveTable("table_sil_w43_2026")
    }

    suspend fun saveQuote(
        clientName: String,
        projectName: String,
        supplierName: String,
        weekRef: String,
        items: List<BudgetItem>,
        discountPercent: Double,
        marginPercent: Double,
        notes: String
    ) {
        val quote = SavedQuote(
            id = UUID.randomUUID().toString(),
            clientName = clientName,
            projectName = projectName,
            createdAt = System.currentTimeMillis(),
            supplierName = supplierName,
            weekRef = weekRef,
            items = items,
            discountPercent = discountPercent,
            marginPercent = marginPercent,
            notes = notes
        )
        savedQuoteDao.insertQuote(quote.toEntity())
    }

    suspend fun deleteQuote(id: String) {
        savedQuoteDao.deleteQuote(id)
    }
}
