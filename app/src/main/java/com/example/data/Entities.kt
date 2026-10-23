package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "supplier_tables")
data class PriceTableEntity(
    @PrimaryKey val id: String,
    val supplierName: String,
    val weekNumber: Int,
    val year: Int,
    val startDate: String,
    val endDate: String,
    val isActive: Boolean,
    val copperAdjustmentPercent: Double,
    val pricesJson: String, // JSON map of String (section) to Double (price)
    val rollDiscountPercent: Double,
    val updatedAt: Long,
    val notes: String
)

@Entity(tableName = "saved_quotes")
data class SavedQuoteEntity(
    @PrimaryKey val id: String,
    val clientName: String,
    val projectName: String,
    val createdAt: Long,
    val supplierName: String,
    val weekRef: String,
    val itemsJson: String,
    val discountPercent: Double,
    val marginPercent: Double,
    val notes: String
)
