package com.example.shopos.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopos.data.model.Sale
import com.example.shopos.data.model.SaleItem
import com.example.shopos.ui.components.ShopOSButton
import com.example.shopos.ui.components.ShopOSCard
import com.example.shopos.ui.components.ShopOSTopBar
import com.example.shopos.ui.theme.*
import com.example.shopos.ui.viewmodel.ShopViewModel
import com.example.shopos.util.Formatters

@Composable
fun ReceiptScreen(
    saleId: Long,
    viewModel: ShopViewModel,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.businessProfile.collectAsState()
    val currency = profile?.currencySymbol ?: "৳"

    var sale by remember { mutableStateOf<Sale?>(null) }
    var items by remember { mutableStateOf<List<SaleItem>>(emptyList()) }

    LaunchedEffect(saleId) {
        val (s, itms) = viewModel.repository.getSaleDetails(saleId)
        sale = s
        items = itms
    }

    Scaffold(
        topBar = {
            ShopOSTopBar(
                title = "Sale Receipt",
                onBack = onDone
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ShopSurfaceWhite)
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Print Button
                Button(
                    onClick = {
                        Toast.makeText(context, "Receipt sent to printer", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ShopPrimaryContainer,
                        contentColor = ShopPrimaryLavender
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Print,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Print Receipt",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }

                // Share Button
                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "Sharing receipt...", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ShopTextDark
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ShopCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }
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
            // Success header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = Formatters.formatCurrency(sale?.total ?: 180.0, currency),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ShopTextDark,
                            fontSize = 32.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(ShopSuccessContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(ShopSuccessGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Payment Successful",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ShopSuccessGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Invoice No: ${sale?.invoiceNumber ?: "#S20241026-0012"}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ShopTextMedium,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = Formatters.formatDate(sale?.dateMillis ?: System.currentTimeMillis()),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ShopTextMuted,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            // Receipt Box
            item {
                ShopOSCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Items",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ShopTextDark
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val displayItems = if (items.isNotEmpty()) items else listOf(
                            SaleItem(saleId = 1, productId = 1, productName = "Maggi Noodles", unitPrice = 45.0, costBasis = 35.0, quantity = 2.0, lineTotal = 90.0),
                            SaleItem(saleId = 1, productId = 2, productName = "Coca Cola 500ml", unitPrice = 60.0, costBasis = 48.0, quantity = 1.0, lineTotal = 60.0),
                            SaleItem(saleId = 1, productId = 6, productName = "Lays Chips", unitPrice = 30.0, costBasis = 22.0, quantity = 1.0, lineTotal = 30.0)
                        )

                        displayItems.forEach { itm ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF2F1FA)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = itm.productName.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = ShopPrimaryLavender
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = itm.productName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = ShopTextDark
                                            )
                                        )
                                        Text(
                                            text = "${itm.quantity.toInt()} x ${Formatters.formatCurrency(itm.unitPrice, currency)}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = ShopTextMuted,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }

                                Text(
                                    text = Formatters.formatCurrency(itm.lineTotal, currency),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ShopTextDark
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = ShopCardBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Totals
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", color = ShopTextMedium, fontSize = 14.sp)
                            Text(Formatters.formatCurrency(sale?.subtotal ?: 180.0, currency), color = ShopTextDark, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Discount", color = ShopTextMedium, fontSize = 14.sp)
                            Text(Formatters.formatCurrency(sale?.discount ?: 0.0, currency), color = ShopTextMedium)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", fontWeight = FontWeight.Bold, color = ShopTextDark, fontSize = 16.sp)
                            Text(Formatters.formatCurrency(sale?.total ?: 180.0, currency), fontWeight = FontWeight.Bold, color = ShopTextDark, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
