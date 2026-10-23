package com.example.shopos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopos.ui.components.ShopOSCard
import com.example.shopos.ui.components.ShopOSFilterChip
import com.example.shopos.ui.components.ShopOSTopBar
import com.example.shopos.ui.theme.*
import com.example.shopos.ui.viewmodel.ShopViewModel
import com.example.shopos.util.Formatters

@Composable
fun ReportsScreen(
    viewModel: ShopViewModel,
    onNavigateToSalesReport: () -> Unit,
    onNavigateToProfitLoss: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val sales by viewModel.sales.collectAsState()
    val saleItems by viewModel.saleItems.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val profile by viewModel.businessProfile.collectAsState()
    val currency = profile?.currencySymbol ?: "৳"

    var selectedPreset by remember { mutableStateOf("30 Days") }
    val presets = listOf("7 Days", "30 Days", "3 Months", "Custom")

    val cutoffMillis = remember(selectedPreset) {
        val now = System.currentTimeMillis()
        when (selectedPreset) {
            "7 Days" -> now - 7L * 24 * 60 * 60 * 1000
            "30 Days" -> now - 30L * 24 * 60 * 60 * 1000
            "3 Months" -> now - 90L * 24 * 60 * 60 * 1000
            else -> 0L
        }
    }

    val periodSales = remember(sales, cutoffMillis) {
        sales.filter { it.dateMillis >= cutoffMillis }
    }
    val periodExpenses = remember(expenses, cutoffMillis) {
        expenses.filter { it.dateMillis >= cutoffMillis }
    }

    val periodSalesTotal = remember(periodSales) { periodSales.sumOf { it.total } }
    val periodExpenseTotal = remember(periodExpenses) { periodExpenses.sumOf { it.amount } }

    val periodSaleIds = remember(periodSales) { periodSales.map { it.id }.toSet() }
    val periodCOGS = remember(saleItems, periodSaleIds) {
        saleItems.filter { it.saleId in periodSaleIds }.sumOf { it.costBasis * it.quantity }
    }
    val periodProfit = (periodSalesTotal - periodCOGS - periodExpenseTotal)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ShopPearlBackground),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ShopOSTopBar(
                title = "Reports",
                onBack = onBack,
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = ShopTextDark
                        )
                    }
                }
            )
        }

        // Date Presets
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presets) { preset ->
                    ShopOSFilterChip(
                        label = preset,
                        selected = selectedPreset == preset,
                        onClick = { selectedPreset = preset }
                    )
                }
            }
        }

        // 2x2 Metric Cards (Strictly live DB values)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReportMetricCard(
                        title = "Total Sales",
                        value = Formatters.formatCurrency(periodSalesTotal, currency),
                        delta = "${periodSales.size} sales",
                        isPositive = periodSalesTotal > 0,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToSalesReport
                    )
                    ReportMetricCard(
                        title = "Cost of Goods",
                        value = Formatters.formatCurrency(periodCOGS, currency),
                        delta = if (periodSalesTotal > 0) "${((periodCOGS / periodSalesTotal) * 100).toInt()}%" else "0%",
                        isPositive = true,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToProfitLoss
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReportMetricCard(
                        title = "Net Profit",
                        value = Formatters.formatCurrency(periodProfit, currency),
                        delta = if (periodProfit >= 0) "Profit" else "Loss",
                        isPositive = periodProfit >= 0,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToProfitLoss
                    )
                    ReportMetricCard(
                        title = "Expense",
                        value = Formatters.formatCurrency(periodExpenseTotal, currency),
                        delta = "${periodExpenses.size} items",
                        isPositive = false,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToProfitLoss
                    )
                }
            }
        }

        // Quick Navigation to deep reports
        item {
            Text(
                text = "Detailed Reports",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ShopTextDark
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            ShopOSCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToSalesReport
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ShopPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.BarChart,
                                contentDescription = null,
                                tint = ShopPrimaryLavender,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Sales Report", fontWeight = FontWeight.SemiBold, color = ShopTextDark)
                            Text("Daily, weekly & product volume breakdown", fontSize = 12.sp, color = ShopTextMuted)
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = ShopTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            ShopOSCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToProfitLoss
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFEF2F2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PieChart,
                                contentDescription = null,
                                tint = ShopBlushCoral,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Profit & Loss", fontWeight = FontWeight.SemiBold, color = ShopTextDark)
                            Text("Revenue, COGS & operating margin", fontSize = 12.sp, color = ShopTextMuted)
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = ShopTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ReportMetricCard(
    title: String,
    value: String,
    delta: String,
    isPositive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ShopOSCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ShopTextMedium,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ShopTextDark,
                    fontSize = 17.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (isPositive) ShopSuccessGreen else ShopBlushCoral,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = delta,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isPositive) ShopSuccessGreen else ShopBlushCoral,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
