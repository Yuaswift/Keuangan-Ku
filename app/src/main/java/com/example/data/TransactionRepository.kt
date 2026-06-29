package com.example.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allBudgets: Flow<List<Budget>> = budgetDao.getAllBudgets()

    suspend fun insert(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun delete(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteById(id: Int) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun insertBudget(budget: Budget) {
        budgetDao.insertBudget(budget)
    }

    suspend fun deleteBudgetByCategory(category: String) {
        budgetDao.deleteBudgetByCategory(category)
    }
}
