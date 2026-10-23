package com.example.shopos.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopos.ui.components.ShopOSCard
import com.example.shopos.ui.components.ShopOSTopBar
import com.example.shopos.ui.theme.*
import com.example.shopos.ui.viewmodel.ShopViewModel
import com.example.shopos.util.Formatters
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SalesReportScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sales by viewModel.sales.collectAsState()
    val saleItems by viewModel.saleItems.collectAsState()
    val profile by viewModel.businessProfile.collectAsState()
    val currency = profile?.currencySymbol ?: "৳"

    var selectedFrequency by remember { mutableStateOf("Daily") }
    val frequencies = listOf("Daily", "Weekly", "Monthly")

    // Real aggregation of past 7 days
    val last7DaysSales = remember(sales) {
        val sdfDay = SimpleDateFormat("dd", Locale.US)
        val cal = Calendar.getInstance()
        val days = mutableListOf<Pair<String, Double>>()

        for (i in 6 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            c.set(Calendar.HOUR_OF_DAY, 0)
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            val dayStart = c.timeInMillis

            c.set(Calendar.HOUR_OF_DAY, 23)
            c.set(Calendar.MINUTE, 59)
            c.set(Calendar.SECOND, 59)
            val dayEnd = c.timeInMillis

            val dayTotal = sales.filter { it.dateMillis in dayStart..dayEnd }.sumOf { it.total }
            days.add(sdfDay.format(Date(dayStart)) to dayTotal)
        }
        days
    }

    val totalVolume = remember(sales) { sales.sumOf { it.total } }

    val topProducts = remember(saleItems, currency) {
        saleItems.groupBy { it.productName }
            .map { (name, items) ->
                val totalQty = items.sumOf { it.quantity }.toInt()
                val unitPrice = items.firstOrNull()?.unitPrice ?: 0.0
                Triple(name, totalQty, Formatters.formatCurrency(unitPrice, currency))
            }
            .sortedByDescending { it.second }
    }

    Scaffold(
        topBar = {
            ShopOSTopBar(
                title = "Sales Report",
                onBack = onBack,
                actions = {
                    IconButton(onClick = {
                        val csv = StringBuilder("Invoice,Date,Customer,Total,PaymentMethod\n")
                        sales.forEach { s ->
                            csv.append("${s.invoiceNumber},${Formatters.formatDate(s.dateMillis)},\"${s.customerName}\",${s.total},${s.paymentMethod}\n")
                        }
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, csv.toString())
                            putExtra(Intent.EXTRA_TITLE, "ShopOS_Sales_Report.csv")
                            type = "text/csv"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Export Sales Report"))
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = "Export CSV",
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

            // Sales Chart Card (Real Data)
            item {
                ShopOSCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sales Volume",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ShopTextDark
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ShopTextDark)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = Formatters.formatCurrency(totalVolume, currency),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Custom Bar Chart using real last 7 days sales
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            val maxDayTotal = last7DaysSales.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1.0

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val count = last7DaysSales.size
                                val barWidth = 24.dp.toPx()
                                val totalWidth = size.width
                                val step = totalWidth / count

                                last7DaysSales.forEachIndexed { i, pair ->
                                    val x = i * step + (step - barWidth) / 2
                                    val fraction = (pair.second / maxDayTotal).toFloat().coerceIn(0.05f, 1f)
                                    val barH = (size.height - 30.dp.toPx()) * fraction
                                    val isPeak = pair.second == maxDayTotal && pair.second > 0

                                    drawRoundRect(
                                        color = if (isPeak) ShopPrimaryLavender else Color(0xFFD6D4F8),
                                        topLeft = Offset(x, (size.height - 30.dp.toPx()) - barH),
                                        size = Size(barWidth, barH),
                                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                    )
                                }
                            }

                            // Day labels
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                last7DaysSales.forEach { pair ->
                                    Text(
                                        text = pair.first,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = ShopTextMuted,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Top Selling Products
            item {
                Text(
                    text = "Top Selling Products",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ShopTextDark
                    )
                )
            }

            if (topProducts.isEmpty()) {
                item {
                    Text(
                        text = "No products sold yet.",
                        style = MaterialTheme.typography.bodySmall.copy(color = ShopTextMuted)
                    )
                }
            } else {
                items(topProducts.take(6)) { (name, qtySold, price) ->
                    ShopOSCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFF2F1FA)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = ShopPrimaryLavender
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(name, fontWeight = FontWeight.SemiBold, color = ShopTextDark)
                                    Text(price, fontSize = 12.sp, color = ShopTextMuted)
                                }
                            }

                            Text(
                                text = "$qtySold sold",
                                fontWeight = FontWeight.Bold,
                                color = ShopTextDark,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
