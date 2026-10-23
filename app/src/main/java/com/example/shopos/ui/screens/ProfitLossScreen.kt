package com.example.shopos.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopos.ui.components.ShopOSCard
import com.example.shopos.ui.components.ShopOSTopBar
import com.example.shopos.ui.theme.*
import com.example.shopos.ui.viewmodel.ShopViewModel
import com.example.shopos.util.Formatters

@Composable
fun ProfitLossScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sales by viewModel.sales.collectAsState()
    val saleItems by viewModel.saleItems.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val profile by viewModel.businessProfile.collectAsState()
    val currency = profile?.currencySymbol ?: "৳"

    var selectedFrequency by remember { mutableStateOf("Monthly") }
    val frequencies = listOf("Daily", "Monthly", "Custom")

    val totalSales = remember(sales) { sales.sumOf { it.total } }
    val totalCOGS = remember(saleItems) { saleItems.sumOf { it.costBasis * it.quantity } }
    val totalExpenses = remember(expenses) { expenses.sumOf { it.amount } }
    val grossProfit = (totalSales - totalCOGS)
    val netProfit = (grossProfit - totalExpenses)

    val grandTotal = (totalSales + totalCOGS + totalExpenses).takeIf { it > 0 } ?: 1.0
    val salesFraction = (totalSales / grandTotal).toFloat()
    val cogsFraction = (totalCOGS / grandTotal).toFloat()
    val expenseFraction = (totalExpenses / grandTotal).toFloat()

    Scaffold(
        topBar = {
            ShopOSTopBar(
                title = "Profit & Loss",
                onBack = onBack,
                actions = {
                    IconButton(onClick = {
                        val report = """
ShopOS Profit & Loss Statement
Period: $selectedFrequency
-------------------------------------------
Total Sales (Revenue): ${Formatters.formatCurrency(totalSales, currency)}
Cost of Goods Sold (COGS): ${Formatters.formatCurrency(totalCOGS, currency)}
Gross Profit: ${Formatters.formatCurrency(grossProfit, currency)}
Operating Expenses: ${Formatters.formatCurrency(totalExpenses, currency)}
-------------------------------------------
Net Profit: ${Formatters.formatCurrency(netProfit, currency)}
Margin: ${if (totalSales > 0) "${((netProfit / totalSales) * 100).toInt()}%" else "0%"}
Generated on: ${Formatters.formatDate(System.currentTimeMillis())}
                        """.trimIndent()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, report)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share P&L Statement"))
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = "Export P&L",
                            tint = ShopTextDark
                        )
                    }
                }
            )
        },
        containerColor = ShopPearlBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Frequency Toggle
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEFEFF6))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    frequencies.forEach { freq ->
                        val isSel = selectedFrequency == freq
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) ShopSurfaceWhite else Color.Transparent)
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = freq,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) ShopTextDark else ShopTextMedium
                                )
                            )
                        }
                    }
                }
            }

            // Donut Ring Chart Card (Dynamically computed arcs)
            item {
                ShopOSCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 22.dp.toPx()
                                val diameter = size.minDimension - strokeWidth
                                val arcSize = Size(diameter, diameter)
                                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                                if (totalSales == 0.0 && totalExpenses == 0.0) {
                                    // Empty state ring
                                    drawArc(
                                        color = Color(0xFFE5E7EB),
                                        startAngle = 0f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth)
                                    )
                                } else {
                                    var currentAngle = -90f

                                    // Sales Arc
                                    val salesSweep = (salesFraction * 360f).coerceAtLeast(10f)
                                    drawArc(
                                        color = Color(0xFF635BFF),
                                        startAngle = currentAngle,
                                        sweepAngle = salesSweep,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                    currentAngle += salesSweep

                                    // COGS Arc
                                    val cogsSweep = (cogsFraction * 360f).coerceAtLeast(10f)
                                    drawArc(
                                        color = Color(0xFFFF8B72),
                                        startAngle = currentAngle,
                                        sweepAngle = cogsSweep,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                    currentAngle += cogsSweep

                                    // Expenses Arc
                                    val expSweep = (expenseFraction * 360f).coerceAtLeast(10f)
                                    drawArc(
                                        color = Color(0xFFFF6575),
                                        startAngle = currentAngle,
                                        sweepAngle = expSweep,
                                        useCenter = false,
                                        topLeft = topLeft,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                }
                            }

                            // Center label
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Net Profit",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = ShopTextMuted,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = Formatters.formatCurrency(netProfit, currency),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (netProfit >= 0) ShopTextDark else ShopBlushCoral,
                                        fontSize = 20.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Breakdown Legend Rows with real percentages
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            val salesPct = if (totalSales > 0) 100 else 0
                            val cogsPct = if (totalSales > 0) ((totalCOGS / totalSales) * 100).toInt() else 0
                            val expPct = if (totalSales > 0) ((totalExpenses / totalSales) * 100).toInt() else 0

                            BreakdownRow(
                                color = Color(0xFF635BFF),
                                label = "Sales (Revenue)",
                                percent = "$salesPct%",
                                amount = Formatters.formatCurrency(totalSales, currency)
                            )
                            BreakdownRow(
                                color = Color(0xFFFF8B72),
                                label = "Cost of Goods",
                                percent = "$cogsPct%",
                                amount = Formatters.formatCurrency(totalCOGS, currency)
                            )
                            BreakdownRow(
                                color = Color(0xFFFF6575),
                                label = "Expenses",
                                percent = "$expPct%",
                                amount = Formatters.formatCurrency(totalExpenses, currency)
                            )
                        }
                    }
                }
            }

            // Summary notes
            item {
                ShopOSCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Accounting Summary",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ShopTextDark
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Gross Profit = Sales (${Formatters.formatCurrency(totalSales, currency)}) - COGS (${Formatters.formatCurrency(totalCOGS, currency)}) = ${Formatters.formatCurrency(grossProfit, currency)}.\nOperating Expenses = ${Formatters.formatCurrency(totalExpenses, currency)}.\nRealized Net Profit is ${Formatters.formatCurrency(netProfit, currency)}.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ShopTextMedium,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BreakdownRow(
    color: Color,
    label: String,
    percent: String,
    amount: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = ShopTextDark
                )
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = percent,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ShopTextMuted,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ShopTextDark
                )
            )
        }
    }
}
