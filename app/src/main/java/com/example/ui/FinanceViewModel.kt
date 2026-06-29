package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Transaction
import com.example.data.Budget
import com.example.data.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class MonthlySummary(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netBalance: Double = 0.0
)

data class CategorySummary(
    val categoryName: String,
    val totalAmount: Double,
    val percentage: Float,
    val isExpense: Boolean
)

data class WeeklyChartData(
    val weekNumber: Int, // 1 to 5
    val label: String,   // e.g., "Minggu 1"
    val incomeAmount: Float,
    val expenseAmount: Float
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransactionRepository
    private val authManager = com.example.data.AuthManager(application)

    private val _currentUserEmail = MutableStateFlow(authManager.getCurrentUserEmail())
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = _currentUserEmail
        .map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = authManager.isLoggedIn()
        )

    val currentUserFullName: StateFlow<String> = _currentUserEmail
        .map { authManager.getUserFullName(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = authManager.getUserFullName()
        )

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TransactionRepository(database.transactionDao(), database.budgetDao())
    }

    // Filters: current month and year
    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allBudgets: StateFlow<List<Budget>> = repository.allBudgets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered transaction list for current view
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        allTransactions,
        _selectedMonth,
        _selectedYear
    ) { txList, month, year ->
        txList.filter { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
            cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Summary of Income, Expense, Balance
    val monthlySummary: StateFlow<MonthlySummary> = filteredTransactions
        .map { txList ->
            var income = 0.0
            var expense = 0.0
            txList.forEach { tx ->
                if (tx.type == "INCOME") {
                    income += tx.amount
                } else {
                    expense += tx.amount
                }
            }
            MonthlySummary(
                totalIncome = income,
                totalExpense = expense,
                netBalance = income - expense
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthlySummary())

    // Category summary breakdown (mainly for expenses)
    val categoryExpenseSummary: StateFlow<List<CategorySummary>> = filteredTransactions
        .map { txList ->
            val expensesOnly = txList.filter { it.type == "EXPENSE" }
            val totalExpense = expensesOnly.sumOf { it.amount }
            
            if (totalExpense == 0.0) return@map emptyList<CategorySummary>()

            expensesOnly
                .groupBy { it.category }
                .map { (catName, list) ->
                    val sum = list.sumOf { it.amount }
                    CategorySummary(
                        categoryName = catName,
                        totalAmount = sum,
                        percentage = (sum / totalExpense).toFloat(),
                        isExpense = true
                    )
                }
                .sortedByDescending { it.totalAmount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Weekly summary for drawing custom chart (Weeks 1 to 5)
    val weeklyChartData: StateFlow<List<WeeklyChartData>> = filteredTransactions
        .map { txList ->
            val weeklyMap = mutableMapOf<Int, Pair<Double, Double>>() // week -> (income, expense)
            for (w in 1..5) {
                weeklyMap[w] = Pair(0.0, 0.0)
            }

            txList.forEach { tx ->
                val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
                val day = cal.get(Calendar.DAY_OF_MONTH)
                val week = when {
                    day <= 7 -> 1
                    day <= 14 -> 2
                    day <= 21 -> 3
                    day <= 28 -> 4
                    else -> 5
                }
                
                val current = weeklyMap[week] ?: Pair(0.0, 0.0)
                if (tx.type == "INCOME") {
                    weeklyMap[week] = Pair(current.first + tx.amount, current.second)
                } else {
                    weeklyMap[week] = Pair(current.first, current.second + tx.amount)
                }
            }

            (1..5).map { w ->
                val (inc, exp) = weeklyMap[w] ?: Pair(0.0, 0.0)
                WeeklyChartData(
                    weekNumber = w,
                    label = "Mgg $w",
                    incomeAmount = inc.toFloat(),
                    expenseAmount = exp.toFloat()
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All distinct years available from transaction list, so filters are dynamic
    val availableYears: StateFlow<List<Int>> = allTransactions
        .map { txList ->
            val curYear = Calendar.getInstance().get(Calendar.YEAR)
            val years = txList.map {
                val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                cal.get(Calendar.YEAR)
            }.toSet()
            if (years.isEmpty()) listOf(curYear) else (years + curYear).sorted()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(Calendar.getInstance().get(Calendar.YEAR)))

    fun setMonth(month: Int) {
        _selectedMonth.value = month
    }

    fun setYear(year: Int) {
        _selectedYear.value = year
    }

    fun login(email: String, password: String): Boolean {
        val user = authManager.login(email, password)
        if (user != null) {
            _currentUserEmail.value = user.email
            return true
        }
        return false
    }

    fun register(fullName: String, email: String, password: String, question: String, answer: String): Boolean {
        return authManager.registerUser(fullName, email, password, question, answer)
    }

    fun getSecurityQuestion(email: String): String? {
        return authManager.getSecurityQuestion(email)
    }

    fun resetPassword(email: String, answer: String, newPassword: String): Boolean {
        return authManager.resetPassword(email, answer, newPassword)
    }

    fun logout() {
        authManager.logout()
        _currentUserEmail.value = null
    }

    fun setBudget(category: String, amount: Double) {
        viewModelScope.launch {
            repository.insertBudget(Budget(category, amount))
        }
    }

    fun removeBudget(category: String) {
        viewModelScope.launch {
            repository.deleteBudgetByCategory(category)
        }
    }

    fun addTransaction(title: String, amount: Double, type: String, category: String, dateMills: Long, note: String) {
        viewModelScope.launch {
            repository.insert(
                Transaction(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    date = dateMills,
                    note = note
                )
            )
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.delete(transaction)
        }
    }

    // Helper to inject initial Indonesian data so preview looks stunning
    fun checkAndPopulateMockData() {
        viewModelScope.launch {
            // Only populate if database is entirely empty
            allTransactions.take(1).collect { list ->
                if (list.isEmpty()) {
                    val cal = Calendar.getInstance()
                    val currentMonth = cal.get(Calendar.MONTH)
                    val currentYear = cal.get(Calendar.YEAR)

                    fun dateAtDay(day: Int): Long {
                        val c = Calendar.getInstance().apply {
                            set(Calendar.YEAR, currentYear)
                            set(Calendar.MONTH, currentMonth)
                            set(Calendar.DAY_OF_MONTH, day)
                            set(Calendar.HOUR_OF_DAY, 12)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                        }
                        return c.timeInMillis
                    }

                    // Populate 10 elegant Indonesian entries spread across weeks
                    val sampleTxs = listOf(
                        Transaction(title = "Gaji Utama Bulanan", amount = 8500000.0, type = "INCOME", category = "Gaji", date = dateAtDay(1), note = "Gaji bulanan kantor"),
                        Transaction(title = "Beli Makan Siang Bakso", amount = 42000.0, type = "EXPENSE", category = "Makanan", date = dateAtDay(3), note = "Makan bareng tim"),
                        Transaction(title = "Isi Bensin Motor", amount = 50000.0, type = "EXPENSE", category = "Transportasi", date = dateAtDay(5), note = "Pertalite full tank"),
                        Transaction(title = "Wifi & Listrik Rumah", amount = 480000.0, type = "EXPENSE", category = "Tagihan", date = dateAtDay(9), note = "Tagihan Telkom & PLN"),
                        Transaction(title = "Bonus Projek Sampingan", amount = 2200000.0, type = "INCOME", category = "Bisnis", date = dateAtDay(12), note = "Freelance UI Design"),
                        Transaction(title = "Belanja Bulanan Carrefour", amount = 950000.0, type = "EXPENSE", category = "Belanja", date = dateAtDay(15), note = "Stunting sembako bulanan"),
                        Transaction(title = "Nonton Bioskop CGV", amount = 110000.0, type = "EXPENSE", category = "Hiburan", date = dateAtDay(18), note = "Film baru akhir pekan"),
                        Transaction(title = "Beli Vitamin Apotek", amount = 75000.0, type = "EXPENSE", category = "Kesehatan", date = dateAtDay(22), note = "Vitamin C & D"),
                        Transaction(title = "Nongkrong Ngopi Senja", amount = 35000.0, type = "EXPENSE", category = "Makanan", date = dateAtDay(25), note = "Kopi susu gula aren"),
                        Transaction(title = "Uang Saku Transportasi", amount = 45000.0, type = "EXPENSE", category = "Transportasi", date = dateAtDay(29), note = "Gojek pulang-pergi")
                    )

                    sampleTxs.forEach { repository.insert(it) }

                    // Also pre-populate category budgets
                    val sampleBudgets = listOf(
                        Budget("Makanan", 500000.0),
                        Budget("Transportasi", 300000.0),
                        Budget("Belanja", 1500000.0),
                        Budget("Tagihan", 1000000.0),
                        Budget("Hiburan", 300000.0),
                        Budget("Kesehatan", 200000.0),
                        Budget("Lain-lain", 150000.0)
                    )
                    sampleBudgets.forEach { repository.insertBudget(it) }
                }
            }
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FinanceViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
