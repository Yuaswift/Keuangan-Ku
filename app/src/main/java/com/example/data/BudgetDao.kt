package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Query("DELETE FROM budgets WHERE category = :category")
    suspend fun deleteBudgetByCategory(category: String)

    @Query("DELETE FROM budgets")
    suspend fun deleteAllBudgets()
}
