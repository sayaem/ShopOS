package com.example.shopos.ui.screens

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

@Composable
fun ProfitLossScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedFrequency by remember { mutableStateOf("Monthly") }
    val frequencies = listOf("Daily", "Monthly", "Custom")

    Scaffold(
        topBar = {
            ShopOSTopBar(
                title = "Profit & Loss",
                onBack = onBack,
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Exported P&L Statement to Downloads", Toast.LENGTH_SHORT).show()
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

            // Donut Ring Chart Card
            item {
                ShopOSCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Donut Canvas
                        Box(
                            modifier = Modifier.size(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 22.dp.toPx()
                                val diameter = size.minDimension - strokeWidth
                                val arcSize = Size(diameter, diameter)
                                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                                // Sales arc (purple/blue): 0 to 220 deg
                                drawArc(
                                    color = Color(0xFF635BFF),
                                    startAngle = -90f,
                                    sweepAngle = 210f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )

                                // COGS arc (coral/peach): 225 to 300 deg
                                drawArc(
                                    color = Color(0xFFFF8B72),
                                    startAngle = 125f,
                                    sweepAngle = 90f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )

                                // Expenses arc (blush): 305 to 355 deg
                                drawArc(
                                    color = Color(0xFFFF6575),
                                    startAngle = 220f,
                                    sweepAngle = 45f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
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
                                    text = "৳ 24,560",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ShopTextDark,
                                        fontSize = 20.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Breakdown Legend Rows
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            BreakdownRow(
                                color = Color(0xFF635BFF),
                                label = "Sales",
                                percent = "78%",
                                amount = "৳ 1,25,480"
                            )
                            BreakdownRow(
                                color = Color(0xFFFF8B72),
                                label = "Cost of Goods",
                                percent = "52%",
                                amount = "৳ 78,320"
                            )
                            BreakdownRow(
                                color = Color(0xFFFF6575),
                                label = "Expenses",
                                percent = "10%",
                                amount = "৳ 12,450"
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
                            text = "Gross Profit = Net Sales (৳ 1,25,480) - COGS (৳ 78,320) = ৳ 47,160.\nOperating Expenses = ৳ 12,450.\nRealized Net Profit for the period is ৳ 24,560 with an 18% margin.",
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
