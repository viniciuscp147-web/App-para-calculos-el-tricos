package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceTableDao {
    @Query("SELECT * FROM supplier_tables ORDER BY year DESC, weekNumber DESC, updatedAt DESC")
    fun getAllTables(): Flow<List<PriceTableEntity>>

    @Query("SELECT * FROM supplier_tables WHERE isActive = 1 LIMIT 1")
    fun getActiveTableFlow(): Flow<PriceTableEntity?>

    @Query("SELECT * FROM supplier_tables WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveTable(): PriceTableEntity?

    @Query("SELECT * FROM supplier_tables WHERE id = :id LIMIT 1")
    suspend fun getTableById(id: String): PriceTableEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTable(table: PriceTableEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTables(tables: List<PriceTableEntity>)

    @Update
    suspend fun updateTable(table: PriceTableEntity)

    @Query("UPDATE supplier_tables SET isActive = 0")
    suspend fun clearActiveFlag()

    @Query("UPDATE supplier_tables SET isActive = 1 WHERE id = :id")
    suspend fun markActive(id: String)

    @Transaction
    suspend fun setActiveTable(id: String) {
        clearActiveFlag()
        markActive(id)
    }

    @Query("DELETE FROM supplier_tables WHERE id = :id")
    suspend fun deleteTable(id: String)

    @Query("SELECT COUNT(*) FROM supplier_tables")
    suspend fun getCount(): Int
}

@Dao
interface SavedQuoteDao {
    @Query("SELECT * FROM saved_quotes ORDER BY createdAt DESC")
    fun getAllQuotes(): Flow<List<SavedQuoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: SavedQuoteEntity)

    @Query("DELETE FROM saved_quotes WHERE id = :id")
    suspend fun deleteQuote(id: String)
}
