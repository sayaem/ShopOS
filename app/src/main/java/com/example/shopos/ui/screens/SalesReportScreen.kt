package com.example.shopos.ui.screens

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
import com.example.shopos.ui.components.ShopOSFilterChip
import com.example.shopos.ui.components.ShopOSTopBar
import com.example.shopos.ui.theme.*
import com.example.shopos.ui.viewmodel.ShopViewModel

@Composable
fun SalesReportScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedFrequency by remember { mutableStateOf("Daily") }
    val frequencies = listOf("Daily", "Weekly", "Monthly")

    Scaffold(
        topBar = {
            ShopOSTopBar(
                title = "Sales Report",
                onBack = onBack,
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Exported Sales CSV to Downloads", Toast.LENGTH_SHORT).show()
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

            // Sales Chart Card
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
                                    text = "৳ 28,450",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Custom Bar Chart
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val days = listOf("20", "21", "22", "23", "24", "25", "26")
                                val heights = listOf(0.4f, 0.6f, 0.35f, 1.0f, 0.75f, 0.85f, 0.65f)
                                val count = days.size
                                val barWidth = 24.dp.toPx()
                                val totalWidth = size.width
                                val step = totalWidth / count

                                heights.forEachIndexed { i, fraction ->
                                    val x = i * step + (step - barWidth) / 2
                                    val barH = (size.height - 30.dp.toPx()) * fraction
                                    val isPeak = i == 3 // Peak day 23

                                    // Draw bar
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
                                val days = listOf("20", "21", "22", "23", "24", "25", "26")
                                days.forEachIndexed { i, d ->
                                    Text(
                                        text = d,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (i == 3) ShopPrimaryLavender else ShopTextMuted,
                                            fontWeight = if (i == 3) FontWeight.Bold else FontWeight.Normal,
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

            val topProducts = listOf(
                Triple("Maggi Noodles", 32, "৳ 45"),
                Triple("Coca Cola 500ml", 24, "৳ 60"),
                Triple("Lays Chips", 18, "৳ 30"),
                Triple("Dettol Soap", 15, "৳ 45")
            )

            items(topProducts) { (name, qtySold, price) ->
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
                                    text = name.take(1),
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
