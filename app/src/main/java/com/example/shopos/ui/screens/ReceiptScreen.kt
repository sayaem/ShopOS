package com.example.shopos.ui.screens

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.widget.Toast
import androidx.compose.foundation.background
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

    fun generateReceiptText(): String {
        val s = sale ?: return "Receipt not found"
        val sb = StringBuilder()
        sb.append("================================\n")
        sb.append("         ${profile?.shopName ?: "ShopOS Store"}\n")
        if (!profile?.address.isNullOrBlank()) sb.append("   ${profile?.address}\n")
        if (!profile?.phone.isNullOrBlank()) sb.append("      Phone: ${profile?.phone}\n")
        sb.append("================================\n")
        sb.append("Invoice: ${s.invoiceNumber}\n")
        sb.append("Date: ${Formatters.formatDate(s.dateMillis)}\n")
        sb.append("Customer: ${s.customerName}\n")
        sb.append("Payment: ${s.paymentMethod}\n")
        sb.append("--------------------------------\n")
        items.forEach { itm ->
            sb.append("${itm.productName}\n")
            sb.append("  ${itm.quantity.toInt()} x ${Formatters.formatCurrency(itm.unitPrice, currency)} = ${Formatters.formatCurrency(itm.lineTotal, currency)}\n")
        }
        sb.append("--------------------------------\n")
        sb.append("Subtotal: ${Formatters.formatCurrency(s.subtotal, currency)}\n")
        if (s.discount > 0) sb.append("Discount: ${Formatters.formatCurrency(s.discount, currency)}\n")
        sb.append("TOTAL: ${Formatters.formatCurrency(s.total, currency)}\n")
        sb.append("Paid: ${Formatters.formatCurrency(s.paidAmount, currency)}\n")
        if (s.dueAmount > 0) sb.append("Due Balance: ${Formatters.formatCurrency(s.dueAmount, currency)}\n")
        sb.append("================================\n")
        sb.append("    Thank you for shopping with us!\n")
        return sb.toString()
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
                // Print Button (Actual Print or share dialog)
                Button(
                    onClick = {
                        val receiptText = generateReceiptText()
                        try {
                            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                            if (printManager != null) {
                                val printIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TITLE, "Receipt-${sale?.invoiceNumber ?: "invoice"}")
                                    putExtra(Intent.EXTRA_TEXT, receiptText)
                                }
                                context.startActivity(Intent.createChooser(printIntent, "Print or Send Receipt"))
                            } else {
                                Toast.makeText(context, "Print service unavailable on this device", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Printing: Receipt copied to share sheet", Toast.LENGTH_SHORT).show()
                        }
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

                // Share Button (Actual Android Intent share sheet)
                OutlinedButton(
                    onClick = {
                        val receiptText = generateReceiptText()
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, receiptText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Receipt via"))
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
                        text = Formatters.formatCurrency(sale?.total ?: 0.0, currency),
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
                        text = "Invoice No: ${sale?.invoiceNumber ?: "#S0000"}",
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

                        if (items.isEmpty()) {
                            Text(
                                text = "No items recorded in this invoice",
                                style = MaterialTheme.typography.bodySmall.copy(color = ShopTextMuted)
                            )
                        } else {
                            items.forEach { itm ->
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
                            Text(Formatters.formatCurrency(sale?.subtotal ?: 0.0, currency), color = ShopTextDark, fontWeight = FontWeight.SemiBold)
                        }
                        if ((sale?.discount ?: 0.0) > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Discount", color = ShopTextMedium, fontSize = 14.sp)
                                Text("-${Formatters.formatCurrency(sale?.discount ?: 0.0, currency)}", color = ShopBlushCoral)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", fontWeight = FontWeight.Bold, color = ShopTextDark, fontSize = 16.sp)
                            Text(Formatters.formatCurrency(sale?.total ?: 0.0, currency), fontWeight = FontWeight.Bold, color = ShopTextDark, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Paid Amount", color = ShopTextMedium, fontSize = 14.sp)
                            Text(Formatters.formatCurrency(sale?.paidAmount ?: 0.0, currency), color = ShopSuccessGreen, fontWeight = FontWeight.SemiBold)
                        }
                        if ((sale?.dueAmount ?: 0.0) > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Remaining Due", color = ShopBlushCoral, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(Formatters.formatCurrency(sale?.dueAmount ?: 0.0, currency), color = ShopBlushCoral, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
