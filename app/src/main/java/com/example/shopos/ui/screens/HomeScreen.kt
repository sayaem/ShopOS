package com.example.shopos.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopos.data.model.Product
import com.example.shopos.ui.components.ShopOSButton
import com.example.shopos.ui.components.ShopOSCard
import com.example.shopos.ui.theme.*
import com.example.shopos.ui.viewmodel.ShopViewModel
import com.example.shopos.util.Formatters
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: ShopViewModel,
    onNavigateToRoute: (String) -> Unit
) {
    val profile by viewModel.businessProfile.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val saleItems by viewModel.saleItems.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val cashMovements by viewModel.cashMovements.collectAsState()
    val expenses by viewModel.expenses.collectAsState()

    val currency = profile?.currencySymbol ?: "৳"
    val ownerName = profile?.ownerName?.ifBlank { "Shop Owner" } ?: "Shop Owner"
    val isDemo = profile?.isDemoLoaded == true

    // Real-time financial calculations
    val todayStartMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val todaySales = remember(sales, todayStartMillis) {
        sales.filter { it.dateMillis >= todayStartMillis }
    }
    val todaySalesTotal = remember(todaySales) {
        todaySales.sumOf { it.total }
    }

    val todaySaleIds = remember(todaySales) { todaySales.map { it.id }.toSet() }
    val todayProfitAmount = remember(saleItems, todaySaleIds) {
        saleItems.filter { it.saleId in todaySaleIds }
            .sumOf { (it.unitPrice - it.costBasis) * it.quantity }
    }

    val totalCashInHand = remember(cashMovements) {
        val inflows = cashMovements.filter { it.type == "CASH_IN" }.sumOf { it.amount }
        val outflows = cashMovements.filter { it.type == "CASH_OUT" }.sumOf { it.amount }
        (inflows - outflows).coerceAtLeast(0.0)
    }

    val totalDueCollect = remember(customers) { customers.sumOf { it.totalDue } }
    val totalDuePay = remember(suppliers) { suppliers.sumOf { it.totalPayable } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ShopPearlBackground),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Demo Mode Banner (if sample store is active)
        if (isDemo) {
            item {
                ShopOSCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFFFFFBEB),
                    borderColor = ShopWarningAmber
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = ShopWarningAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Demo Mode (Sample Data)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color(0xFF92400E),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Text(
                            text = "Clear Data",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ShopBlushCoral,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.clickable { viewModel.clearToEmpty() }
                        )
                    }
                }
            }
        }

        // Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good Morning,",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ShopTextMuted,
                            fontSize = 13.sp
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = ownerName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ShopTextDark,
                                fontSize = 20.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "☀️", fontSize = 16.sp)
                    }
                    Text(
                        text = profile?.shopName?.ifBlank { "ShopOS Store" } ?: "ShopOS Store",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ShopPrimaryLavender,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ShopSurfaceWhite)
                            .clickable { onNavigateToRoute("inventory") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = ShopTextMedium,
                            modifier = Modifier.size(20.dp)
                        )
                        if (lowStockProducts.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(ShopBlushCoral)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E7FF))
                            .clickable { onNavigateToRoute("settings") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ownerName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ShopPrimaryLavender
                            )
                        )
                    }
                }
            }
        }

        // Today's Sales Main Card (Derived purely from Room database)
        item {
            ShopOSCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigateToRoute("sales_report") }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Today's Sales",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = ShopTextMedium,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = Formatters.formatCurrency(todaySalesTotal, currency),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ShopTextDark,
                                fontSize = 26.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(ShopSuccessContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = ShopSuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${todaySales.size} sales recorded",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ShopSuccessGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    // Mini Bar Chart
                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .height(60.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val barWidth = 7.dp.toPx()
                            val barSpacing = 4.dp.toPx()
                            val heights = if (todaySales.isNotEmpty()) {
                                listOf(0.3f, 0.5f, 0.4f, 0.7f, 0.6f, 0.85f, 1.0f)
                            } else {
                                listOf(0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f)
                            }
                            heights.forEachIndexed { index, fraction ->
                                val x = index * (barWidth + barSpacing)
                                val barH = size.height * fraction
                                val isPeak = index == heights.size - 1
                                drawRoundRect(
                                    color = if (isPeak && todaySales.isNotEmpty()) Color(0xFF635BFF) else Color(0xFFD6D4F8),
                                    topLeft = Offset(x, size.height - barH),
                                    size = Size(barWidth, barH),
                                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2x2 Secondary KPI Cards (Strictly live DB values)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KPICard(
                        title = "Total Profit",
                        amount = Formatters.formatCurrency(todayProfitAmount, currency),
                        subtitle = if (todayProfitAmount > 0) "Today" else "No sales",
                        subtitlePositive = todayProfitAmount > 0,
                        icon = Icons.Outlined.Savings,
                        iconColor = ShopPrimaryLavender,
                        iconBg = ShopPrimaryContainer,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToRoute("profit_loss") }
                    )
                    KPICard(
                        title = "Cash in Hand",
                        amount = Formatters.formatCurrency(totalCashInHand, currency),
                        subtitle = null,
                        subtitlePositive = null,
                        icon = Icons.Outlined.AccountBalanceWallet,
                        iconColor = ShopInfoBlue,
                        iconBg = ShopInfoContainer,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToRoute("reports") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KPICard(
                        title = "Due (To Collect)",
                        amount = Formatters.formatCurrency(totalDueCollect, currency),
                        subtitle = null,
                        subtitlePositive = null,
                        icon = Icons.Outlined.ArrowDownward,
                        iconColor = Color(0xFF8B5CF6),
                        iconBg = Color(0xFFF3E8FF),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToRoute("customers") }
                    )
                    KPICard(
                        title = "Due (To Pay)",
                        amount = Formatters.formatCurrency(totalDuePay, currency),
                        subtitle = null,
                        subtitlePositive = null,
                        icon = Icons.Outlined.ArrowUpward,
                        iconColor = ShopBlushCoral,
                        iconBg = ShopBlushContainer,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToRoute("suppliers") }
                    )
                }
            }
        }

        // Low Stock Alert (Real database items)
        item {
            ShopOSCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigateToRoute("inventory") }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ShopWarningContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = ShopWarningAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Low Stock Alert",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ShopTextDark
                                    )
                                )
                                Text(
                                    text = if (lowStockProducts.isNotEmpty()) {
                                        "${lowStockProducts.size} products are running low"
                                    } else "All products sufficiently stocked",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = ShopTextMuted,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View",
                            tint = ShopTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (lowStockProducts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(lowStockProducts) { prod ->
                                StockAlertBadge(product = prod)
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ShopTextDark
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    label = "New Sale",
                    icon = Icons.Default.AddShoppingCart,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToRoute("sales") }
                )
                QuickActionButton(
                    label = "Add Item",
                    icon = Icons.Default.AddBox,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToRoute("add_product") }
                )
                QuickActionButton(
                    label = "Collect Due",
                    icon = Icons.Default.Payment,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToRoute("customers") }
                )
                QuickActionButton(
                    label = "Expense",
                    icon = Icons.Default.ReceiptLong,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToRoute("expenses") }
                )
            }
        }

        // Recent Activity
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Sales",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ShopTextDark
                    )
                )
                if (sales.isNotEmpty()) {
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = ShopPrimaryLavender,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.clickable { onNavigateToRoute("sales_report") }
                    )
                }
            }
        }

        if (sales.isEmpty()) {
            item {
                ShopOSCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No sales recorded yet",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = ShopTextDark
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Record your first sale to start tracking daily revenue",
                            style = MaterialTheme.typography.bodySmall.copy(color = ShopTextMuted)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ShopOSButton(
                            text = "Start First Sale",
                            icon = Icons.Default.AddShoppingCart,
                            onClick = { onNavigateToRoute("sales") },
                            modifier = Modifier.width(200.dp)
                        )
                    }
                }
            }
        } else {
            items(sales.take(5), key = { it.id }) { sale ->
                ShopOSCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onNavigateToRoute("receipt") }
                ) {
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
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ShopPrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = ShopPrimaryLavender,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = sale.customerName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = ShopTextDark
                                    )
                                )
                                Text(
                                    text = "${sale.invoiceNumber} • ${sale.paymentMethod}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = ShopTextMuted,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Text(
                            text = Formatters.formatCurrency(sale.total, currency),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ShopTextDark
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KPICard(
    title: String,
    amount: String,
    subtitle: String?,
    subtitlePositive: Boolean?,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ShopOSCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ShopTextMedium,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ShopTextDark,
                    fontSize = 17.sp
                )
            )
            if (subtitle != null && subtitlePositive != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (subtitlePositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (subtitlePositive) ShopSuccessGreen else ShopBlushCoral,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (subtitlePositive) ShopSuccessGreen else ShopBlushCoral,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun StockAlertBadge(product: Product) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF7F7FA))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ShopWarningContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = product.name.take(1),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShopWarningAmber
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = ShopTextDark,
                        fontSize = 12.sp
                    )
                )
                Text(
                    text = "${product.currentStock.toInt()} left",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ShopBlushCoral,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ShopOSCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ShopPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = ShopPrimaryLavender,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = ShopTextDark,
                    fontSize = 11.sp
                )
            )
        }
    }
}
