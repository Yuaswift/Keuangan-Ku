package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Transaction
import java.text.SimpleDateFormat
import java.util.*

// Format Rupiah Helper
fun formatRupiah(amount: Double): String {
    val formatter = java.text.DecimalFormat.getCurrencyInstance(Locale("id", "ID"))
    return try {
        val formatted = formatter.format(amount)
        if (formatted.endsWith(",00")) {
            formatted.substring(0, formatted.length - 3)
        } else {
            formatted
        }
    } catch (e: Exception) {
        val dec = java.text.DecimalFormat("#,###")
        dec.decimalFormatSymbols = java.text.DecimalFormatSymbols().apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        "Rp " + dec.format(amount)
    }
}

// Shortened values for chart indicator labels (e.g. 2.5jt, 50rb)
fun formatValueShort(amount: Double): String {
    return when {
        amount >= 1000000.0 -> {
            val formatted = String.format(Locale.US, "%.1f", amount / 1000000.0)
            "${formatted.replace(".0", "")}jt"
        }
        amount >= 1000.0 -> {
            val formatted = String.format(Locale.US, "%.0f", amount / 1000.0)
            "${formatted}rb"
        }
        amount > 0.0 -> {
            amount.toInt().toString()
        }
        else -> "0"
    }
}

// Category list for Income vs Expense
val INCOME_CATEGORIES = listOf("Gaji", "Bisnis", "Investasi", "Lain-lain")
val EXPENSE_CATEGORIES = listOf("Makanan", "Transportasi", "Belanja", "Tagihan", "Hiburan", "Kesehatan", "Lain-lain")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceDashboard(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Gather State Flows from ViewModel
    val currentMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val currentYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val summary by viewModel.monthlySummary.collectAsStateWithLifecycle()
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val categoryExpenses by viewModel.categoryExpenseSummary.collectAsStateWithLifecycle()
    val weeklyChartList by viewModel.weeklyChartData.collectAsStateWithLifecycle()
    val budgets by viewModel.allBudgets.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf("beranda") }
    val listState = rememberLazyListState()

    // On start, populate initial sample data so the app has structured records
    LaunchedEffect(Unit) {
        viewModel.checkAndPopulateMockData()
    }

    // Month Names in Indonesian
    val monthNames = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(80.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Beranda
                    Column(
                        modifier = Modifier
                            .clickable { activeTab = "beranda" }
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Beranda",
                            tint = if (activeTab == "beranda") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Beranda",
                            fontSize = 10.sp,
                            fontWeight = if (activeTab == "beranda") FontWeight.Bold else FontWeight.Medium,
                            color = if (activeTab == "beranda") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    // Laporan
                    Column(
                        modifier = Modifier
                            .clickable { activeTab = "laporan" }
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Laporan",
                            tint = if (activeTab == "laporan") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Laporan",
                            fontSize = 10.sp,
                            fontWeight = if (activeTab == "laporan") FontWeight.Bold else FontWeight.Medium,
                            color = if (activeTab == "laporan") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    // Large custom Add button in center!
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFFD3E4FF), shape = RoundedCornerShape(18.dp))
                            .clickable { showAddDialog = true }
                            .testTag("add_transaction_bottom_nav"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah",
                            tint = Color(0xFF001C38),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Dompet
                    Column(
                        modifier = Modifier
                            .clickable { activeTab = "dompet" }
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Dompet",
                            tint = if (activeTab == "dompet") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Dompet",
                            fontSize = 10.sp,
                            fontWeight = if (activeTab == "dompet") FontWeight.Bold else FontWeight.Medium,
                            color = if (activeTab == "dompet") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    // Profil
                    Column(
                        modifier = Modifier
                            .clickable { activeTab = "profil" }
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Profil",
                            tint = if (activeTab == "profil") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Profil",
                            fontSize = 10.sp,
                            fontWeight = if (activeTab == "profil") FontWeight.Bold else FontWeight.Medium,
                            color = if (activeTab == "profil") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (activeTab == "beranda") {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
            // Elegant top minimal branding header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${monthNames[currentMonth]} $currentYear".uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Ringkasan Saku",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 26.sp
                    )
                }
                // Quick avatar representation matches "AD" / "AM"
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFFD3E4FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AM",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF001C38)
                    )
                }
            }

            // Month & Year Timeline Navigation Row (now placed cleanly under headers)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        if (currentMonth == 0) {
                            viewModel.setMonth(11)
                            viewModel.setYear(currentYear - 1)
                        } else {
                            viewModel.setMonth(currentMonth - 1)
                        }
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), CircleShape)
                        .testTag("prev_month_button")
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Bulan Sebelumnya",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "${monthNames[currentMonth]} $currentYear",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(
                    onClick = {
                        if (currentMonth == 11) {
                            viewModel.setMonth(0)
                            viewModel.setYear(currentYear + 1)
                        } else {
                            viewModel.setMonth(currentMonth + 1)
                        }
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), CircleShape)
                        .testTag("next_month_button")
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "Bulan Selanjutnya",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Balance Summary Card
                    item {
                        BalanceCard(summary)
                    }

                    // 2. Custom Canvas Weekly Chart
                    item {
                        ChartCard(
                            weeklyData = weeklyChartList,
                            onAddMockData = { viewModel.checkAndPopulateMockData() }
                        )
                    }

                    // 3. Category Expenses breakdown
                    if (categoryExpenses.isNotEmpty()) {
                        item {
                            CategoryBreakdownCard(categoryExpenses)
                        }
                    }

                    // Budgeting progress card
                    item {
                        BudgetCard(
                            allBudgets = budgets,
                            transactions = transactions,
                            onManageClick = { showBudgetDialog = true }
                        )
                    }

                    // 4. Monthly Transactions List Header
                    item {
                        Text(
                            text = "Riwayat Transaksi",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // 5. Monthly Transactions List Items
                    if (transactions.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Belum Ada Transaksi",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Belum Ada Catatan Transaksi",
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Wah, bulan ini masih kosong. Ketuk tombol + untuk menambahkan catatan atau gunakan tombol muat data contoh di atas.",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    } else {
                        items(transactions, key = { it.id }) { tx ->
                            TransactionItem(
                                transaction = tx,
                                onDelete = { viewModel.deleteTransaction(tx) }
                            )
                        }
                    }

                    // Extra margin at the bottom so elements do not overlap with floating button
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }

                // Smooth vertical scrollbar indicator line
                VerticalScrollbar(
                    lazyListState = listState,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp, top = 4.dp, bottom = 4.dp)
                        .width(6.dp)
                        .fillMaxHeight()
                )
            }
        }
    } else if (activeTab == "profil") {
        ProfileScreen(
            viewModel = viewModel,
            onLogoutClick = {
                viewModel.logout()
            }
        )
    } else if (activeTab == "laporan") {
        ReportsScreen(
            viewModel = viewModel
        )
    } else if (activeTab == "dompet") {
        WalletsScreen(
            viewModel = viewModel
        )
    }
}
}


    // Modal Input Dialog
    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, amount, type, category, date, note ->
                viewModel.addTransaction(title, amount, type, category, date, note)
                showAddDialog = false
            }
        )
    }

    // Modal Budget Dialog
    if (showBudgetDialog) {
        ManageBudgetsDialog(
            allBudgets = budgets,
            onDismiss = { showBudgetDialog = false },
            onSaveBudget = { category, amount ->
                viewModel.setBudget(category, amount)
            },
            onRemoveBudget = { category ->
                viewModel.removeBudget(category)
            }
        )
    }
}

@Composable
fun BalanceCard(summary: MonthlySummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Saldo Bulan Ini",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatRupiah(summary.netBalance),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (summary.netBalance >= 0) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Income flow
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.KeyboardArrowUp,
                            contentDescription = "Pemasukan",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Pemasukan",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formatRupiah(summary.totalIncome),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                // Split/Divider line
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                )

                // Expense flow
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Pengeluaran",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Pengeluaran",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formatRupiah(summary.totalExpense),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChartCard(
    weeklyData: List<WeeklyChartData>,
    onAddMockData: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Grafik Keuangan Bulanan",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Indicators Legend
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF2E7D32), CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Masuk", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFFD32F2F), CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Keluar", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val maxVal = weeklyData.maxOfOrNull { maxOf(it.incomeAmount, it.expenseAmount) } ?: 0f
            
            if (maxVal == 0f) {
                // If there are no mock data / transactions, show helpful loader
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Grafik kosong karena pengeluaran dan pemasukan Rp 0",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onAddMockData) {
                        Text("Muat Data Contoh")
                    }
                }
            } else {
                // Customized elegant canvas bar-chart
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val canvasHeight = size.height
                    val canvasWidth = size.width
                    
                    // Padding boundaries
                    val bottomPadding = 24.dp.toPx()
                    val topPadding = 12.dp.toPx()
                    val chartHeight = canvasHeight - bottomPadding - topPadding
                    val barWidth = 14.dp.toPx()
                    val columnGap = 16.dp.toPx() // gap between dual bars
                    
                    val stepX = canvasWidth / 5f // 5 columns
                    val valScale = if (maxVal > 0f) chartHeight / maxVal else 1f

                    // Draw 3 background horizontal dotted helpers
                    val linePaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.LTGRAY
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = 1.dp.toPx()
                        pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f)
                    }

                    for (i in 1..3) {
                        val y = topPadding + (chartHeight * (i - 1) / 3f)
                        this.drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawLine(0f, y, canvasWidth, y, linePaint)
                        }
                    }

                    weeklyData.forEachIndexed { index, item ->
                        val centerX = (stepX * index) + (stepX / 2f)
                        
                        // Y coordinate calculation (from bottom going up)
                        val incomeHeight = item.incomeAmount * valScale
                        val expenseHeight = item.expenseAmount * valScale

                        val incomeY = canvasHeight - bottomPadding - incomeHeight
                        val expenseY = canvasHeight - bottomPadding - expenseHeight

                        // 1. Draw Income Bar (Green)
                        if (item.incomeAmount > 0f) {
                            val incomeLeft = centerX - barWidth - (columnGap / 4f)
                            drawRoundRect(
                                color = Color(0xFF2E7D32),
                                topLeft = Offset(incomeLeft, incomeY),
                                size = Size(barWidth, incomeHeight),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                            
                            // Draw value tag on top of income bar
                            drawIntoCanvas { canvas ->
                                val textPaint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.parseColor("#1B5E20")
                                    textSize = 8.dp.toPx()
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                                }
                                canvas.nativeCanvas.drawText(
                                    formatValueShort(item.incomeAmount.toDouble()),
                                    incomeLeft + (barWidth / 2f),
                                    incomeY - 4.dp.toPx(),
                                    textPaint
                                )
                            }
                        }

                        // 2. Draw Expense Bar (Coral Red)
                        if (item.expenseAmount > 0f) {
                            val expenseLeft = centerX + (columnGap / 4f)
                            drawRoundRect(
                                color = Color(0xFFD32F2F),
                                topLeft = Offset(expenseLeft, expenseY),
                                size = Size(barWidth, expenseHeight),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )

                            // Draw value tag on top of expense bar
                            drawIntoCanvas { canvas ->
                                val textPaint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.parseColor("#B71C1C")
                                    textSize = 8.dp.toPx()
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                                }
                                canvas.nativeCanvas.drawText(
                                    formatValueShort(item.expenseAmount.toDouble()),
                                    expenseLeft + (barWidth / 2f),
                                    expenseY - 4.dp.toPx(),
                                    textPaint
                                )
                            }
                        }

                        // 3. Draw X-Axis labels
                        drawIntoCanvas { canvas ->
                            val labelPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.DKGRAY
                                textSize = 9.dp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                            canvas.nativeCanvas.drawText(
                                item.label,
                                centerX,
                                canvasHeight - 6.dp.toPx(),
                                labelPaint
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBreakdownCard(breakdown: List<CategorySummary>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Kategori Pengeluaran Terbesar",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            breakdown.forEach { cat ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cat.categoryName,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatRupiah(cat.totalAmount),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${(cat.percentage * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { cat.percentage },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(4.dp)
                            ),
                        color = Color(0xFFD32F2F),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    onDelete: () -> Unit
) {
    val isExpense = transaction.type == "EXPENSE"
    
    // Choose icons for categories securely using mapping
    val categoryIcon = when (transaction.category) {
        "Gaji" -> Icons.Default.Star
        "Bisnis" -> Icons.Default.Home
        "Investasi" -> Icons.Default.ThumbUp
        "Makanan" -> Icons.Default.Favorite
        "Transportasi" -> Icons.Default.LocationOn
        "Belanja" -> Icons.Default.ShoppingCart
        "Tagihan" -> Icons.Default.DateRange
        "Hiburan" -> Icons.Default.Notifications
        "Kesehatan" -> Icons.Default.Info
        else -> Icons.Default.Menu
    }

    val iconColor = if (isExpense) Color(0xFFD32F2F) else Color(0xFF2E7D32)
    val iconBgColor = if (isExpense) Color(0xFFD32F2F).copy(alpha = 0.1f) else Color(0xFF2E7D32).copy(alpha = 0.1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rounded icon framework
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color = iconBgColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = transaction.category,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text description group
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = transaction.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )
                    val dateFormatted = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                        .format(Date(transaction.date))
                    Text(
                        text = dateFormatted,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                if (transaction.note.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = transaction.note,
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Flow balance status & delete button
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = (if (isExpense) "-" else "+") + formatRupiah(transaction.amount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = iconColor
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("delete_${transaction.id}")
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Hapus Transaksi",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, type: String, category: String, dateMills: Long, note: String) -> Unit
) {
    val context = LocalContext.current
    
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("EXPENSE") } // "EXPENSE" or "INCOME"
    var selectedCategory by remember { mutableStateOf("Makanan") }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }

    var isTitleError by remember { mutableStateOf(false) }
    var isAmountError by remember { mutableStateOf(false) }

    // Synchronize category list whenever type updates
    LaunchedEffect(type) {
        selectedCategory = if (type == "INCOME") INCOME_CATEGORIES.first() else EXPENSE_CATEGORIES.first()
    }

    val dateFormatted = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        .format(selectedDate.time)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Catat Transaksi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Segmented Type Toggles (Pemasukan vs Pengeluaran)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (type == "EXPENSE") MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { type = "EXPENSE" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Pengeluaran",
                            fontWeight = FontWeight.SemiBold,
                            color = if (type == "EXPENSE") Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (type == "INCOME") Color(0xFF2E7D32).copy(alpha = 0.15f) else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { type = "INCOME" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Pemasukan",
                            fontWeight = FontWeight.SemiBold,
                            color = if (type == "INCOME") Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (isTitleError) isTitleError = false
                    },
                    label = { Text("Judul Transaksi") },
                    placeholder = { Text("Contoh: Makan Siang Nasi Padang") },
                    isError = isTitleError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("input_title")
                )

                // Amount Input
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it
                        if (isAmountError) isAmountError = false
                    },
                    label = { Text("Nominal (Rp)") },
                    placeholder = { Text("Contoh: 35000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isAmountError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("input_amount")
                )

                // Dynamic Category Grid/Wrap selection
                Text(
                    text = "Kategori",
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                val categoriesList = if (type == "INCOME") INCOME_CATEGORIES else EXPENSE_CATEGORIES
                
                // Wrap categories in dynamic flow layouts or horizontal scrolling row to be stylish
                ScrollableTabRow(
                    selectedTabIndex = categoriesList.indexOf(selectedCategory).coerceAtLeast(0),
                    edgePadding = 0.dp,
                    indicator = {},
                    divider = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    categoriesList.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        Card(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable { selectedCategory = cat },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                }
                            )
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Date Picker trigger Button
                OutlinedButton(
                    onClick = {
                        val dpd = DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    set(Calendar.HOUR_OF_DAY, 12)
                                }
                                selectedDate = cal
                            },
                            selectedDate.get(Calendar.YEAR),
                            selectedDate.get(Calendar.MONTH),
                            selectedDate.get(Calendar.DAY_OF_MONTH)
                        )
                        dpd.show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.DateRange, contentDescription = "Pilih Tanggal")
                        Text(dateFormatted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                // Optional Notes Input
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan Tambahan (Opsional)") },
                    placeholder = { Text("Contoh: Makan siang traktiran ultah") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .testTag("input_note")
                )

                // Dialog Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_cancel_dialog")
                    ) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cleanTitle = title.trim()
                            val cleanAmount = amountStr.toDoubleOrNull() ?: 0.0

                            var fail = false
                            if (cleanTitle.isEmpty()) {
                                isTitleError = true
                                fail = true
                            }
                            if (cleanAmount <= 0.0) {
                                isAmountError = true
                                fail = true
                            }

                            if (!fail) {
                                onSave(cleanTitle, cleanAmount, type, selectedCategory, selectedDate.timeInMillis, note)
                            }
                        },
                        modifier = Modifier.testTag("btn_save_dialog")
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetCard(
    allBudgets: List<com.example.data.Budget>,
    transactions: List<Transaction>,
    onManageClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("budget_progress_card"),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Anggaran Bulanan",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Status pengeluaran saku Anda",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                TextButton(
                    onClick = onManageClick,
                    modifier = Modifier.testTag("manage_budgets_button"),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Atur",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Atur",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val expenseTransactions = transactions.filter { it.type == "EXPENSE" }
            val categorySpending = expenseTransactions.groupBy { it.category }.mapValues { entry ->
                entry.value.sumOf { it.amount }
            }

            val expenseCategories = listOf("Makanan", "Transportasi", "Belanja", "Tagihan", "Hiburan", "Kesehatan", "Lain-lain")

            var hasAnyBudget = false
            expenseCategories.forEach { categoryName ->
                val budgetAmount = allBudgets.find { it.category == categoryName }?.amount ?: 0.0
                if (budgetAmount > 0) {
                    hasAnyBudget = true
                }
            }

            if (!hasAnyBudget) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Belum ada anggaran diatur",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ketuk 'Atur' di atas untuk mengatur batas belanja bulanan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    expenseCategories.forEach { categoryName ->
                        val budgetAmount = allBudgets.find { it.category == categoryName }?.amount ?: 0.0
                        if (budgetAmount > 0.0) {
                            val spentAmount = categorySpending[categoryName] ?: 0.0
                            val ratio = if (budgetAmount > 0.0) (spentAmount / budgetAmount).toFloat() else 0f
                            val progress = ratio.coerceAtMost(1f)

                            val categoryIcon = when (categoryName) {
                                "Makanan" -> Icons.Default.Favorite
                                "Transportasi" -> Icons.Default.LocationOn
                                "Belanja" -> Icons.Default.ShoppingCart
                                "Tagihan" -> Icons.Default.DateRange
                                "Hiburan" -> Icons.Default.Notifications
                                "Kesehatan" -> Icons.Default.Info
                                else -> Icons.Default.Menu
                            }

                            val progressColor = when {
                                ratio <= 0.8f -> Color(0xFF2E7D32) // Emerald Green
                                ratio <= 1.0f -> Color(0xFFF9A825) // Amber
                                else -> Color(0xFFD32F2F)        // Red
                            }

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = categoryIcon,
                                            contentDescription = categoryName,
                                            tint = progressColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = categoryName,
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${formatRupiah(spentAmount)} / ${formatRupiah(budgetAmount)}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (spentAmount > budgetAmount) {
                                                "Lebih ${formatRupiah(spentAmount - budgetAmount)}"
                                            } else {
                                                "Sisa ${formatRupiah(budgetAmount - spentAmount)}"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (spentAmount > budgetAmount) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = progress,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    color = progressColor,
                                    trackColor = progressColor.copy(alpha = 0.15f),
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageBudgetsDialog(
    allBudgets: List<com.example.data.Budget>,
    onDismiss: () -> Unit,
    onSaveBudget: (category: String, amount: Double) -> Unit,
    onRemoveBudget: (category: String) -> Unit
) {
    val expenseCategories = listOf("Makanan", "Transportasi", "Belanja", "Tagihan", "Hiburan", "Kesehatan", "Lain-lain")
    
    val budgetMap = remember(allBudgets) {
        val map = androidx.compose.runtime.mutableStateMapOf<String, String>()
        expenseCategories.forEach { name ->
            val amt = allBudgets.find { it.category == name }?.amount
            map[name] = if (amt != null && amt > 0.0) amt.toInt().toString() else ""
        }
        map
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 16.dp)
                .testTag("manage_budgets_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Atur Anggaran Bulanan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tentukan batas pengeluaran maksimum untuk masing-masing kategori.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(max = 350.dp)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(expenseCategories) { categoryName ->
                            val categoryIcon = when (categoryName) {
                                "Makanan" -> Icons.Default.Favorite
                                "Transportasi" -> Icons.Default.LocationOn
                                "Belanja" -> Icons.Default.ShoppingCart
                                "Tagihan" -> Icons.Default.DateRange
                                "Hiburan" -> Icons.Default.Notifications
                                "Kesehatan" -> Icons.Default.Info
                                else -> Icons.Default.Menu
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = categoryIcon,
                                        contentDescription = categoryName,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = categoryName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = budgetMap[categoryName] ?: "",
                                        onValueChange = { newVal ->
                                            if (newVal.all { it.isDigit() }) {
                                                budgetMap[categoryName] = newVal
                                            }
                                        },
                                        placeholder = { Text("Tanpa Batas", fontSize = 13.sp) },
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyMedium,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("budget_input_$categoryName"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                            focusedBorderColor = MaterialTheme.colorScheme.primary
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_budget_button")
                    ) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            budgetMap.forEach { (cat, valueStr) ->
                                val amt = valueStr.toDoubleOrNull() ?: 0.0
                                if (amt > 0.0) {
                                    onSaveBudget(cat, amt)
                                } else {
                                    onRemoveBudget(cat)
                                }
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("save_budget_button")
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    viewModel: FinanceViewModel,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    val fullName by viewModel.currentUserFullName.collectAsStateWithLifecycle()
    val email by viewModel.currentUserEmail.collectAsStateWithLifecycle()
    val summary by viewModel.monthlySummary.collectAsStateWithLifecycle()
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()

    val totalIncome = summary.totalIncome
    val totalExpense = summary.totalExpense
    val savingsCount = transactions.size

    val initials = remember(fullName) {
        if (fullName.isEmpty()) "PK" else {
            val parts = fullName.trim().split(" ")
            if (parts.size >= 2) {
                "${parts[0].firstOrNull() ?: 'U'}${parts[1].firstOrNull() ?: 'P'}".uppercase()
            } else {
                "${fullName.firstOrNull() ?: 'U'}".uppercase()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Center Avatar
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(Color(0xFFD3E4FF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = Color(0xFF001C38)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Full Name and email
        Text(
            text = fullName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = email ?: "user@example.com",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Account Activity Stats Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Aktivitas Keuangan Bulan Ini",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total Pemasukan",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = formatRupiah(totalIncome),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Pengeluaran",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = formatRupiah(totalExpense),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Jumlah Transaksi Terdaftar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$savingsCount transaksi",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Informasi Aplikasi",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Versi Aplikasi", style = MaterialTheme.typography.bodySmall)
                    Text(text = "v1.2.0-Offline", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Database", style = MaterialTheme.typography.bodySmall)
                    Text(text = "SQLite Room", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Custom Red Logout buttons
        Button(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("logout_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC62828),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Keluar",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Keluar Akun (Sign Out)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun ReportsScreen(viewModel: FinanceViewModel) {
    val summary by viewModel.monthlySummary.collectAsStateWithLifecycle()
    val categoryExpenses by viewModel.categoryExpenseSummary.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Laporan Analisis",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Peta rincian pengeluaran saku bulanan dari kategori terpilih",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        BalanceCard(summary = summary)

        if (categoryExpenses.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "No data",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Belum Ada Pengeluaran",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Silakan catat beberapa pengeluaran di halaman beranda terlebih dahulu untuk melihat grafik analisis terarah.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            CategoryBreakdownCard(breakdown = categoryExpenses)
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun WalletsScreen(viewModel: FinanceViewModel) {
    val summary by viewModel.monthlySummary.collectAsStateWithLifecycle()

    val walletSources = listOf(
        Triple("Dompet Utama", "Saldo Kas Tunai", 0.3f),
        Triple("Rekening Bank Transfer", "Bank Mandiri / BCA", 0.6f),
        Triple("Pos Saku E-Wallet", "GoPay / OVO", 0.1f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Dompet Keuangan",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Atur pos alokasi dana dan sumber saldo rekening harian Anda",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        walletSources.forEach { (name, note, ratio) ->
            val amount = summary.netBalance.coerceAtLeast(0.0) * ratio
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = name,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                    Text(
                        text = formatRupiah(amount),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun VerticalScrollbar(
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
) {
    val layoutInfo = lazyListState.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    val totalItemsCount = layoutInfo.totalItemsCount
    
    if (totalItemsCount <= 1 || visibleItems.isEmpty()) return

    val showScrollbar = visibleItems.size < totalItemsCount

    if (showScrollbar) {
        val estimatedTotalHeight = totalItemsCount.toFloat()
        val firstVisibleIndex = lazyListState.firstVisibleItemIndex.toFloat()
        val visibleItemsCount = visibleItems.size.toFloat()
        
        val scrollbarHeightFraction = (visibleItemsCount / estimatedTotalHeight).coerceIn(0.15f, 1.0f)
        val scrollbarOffsetFraction = (firstVisibleIndex / estimatedTotalHeight).coerceIn(0f, 1f - scrollbarHeightFraction)

        Box(
            modifier = modifier
                .fillMaxHeight()
                .width(6.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(scrollbarHeightFraction)
                    .align(Alignment.TopCenter)
                    .graphicsLayer {
                        translationY = scrollbarOffsetFraction * size.height
                    }
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
            )
        }
    }
}
