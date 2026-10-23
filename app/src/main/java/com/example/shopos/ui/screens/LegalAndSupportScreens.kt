package com.example.shopos.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopos.ui.components.ShopOSButton
import com.example.shopos.ui.components.ShopOSCard
import com.example.shopos.ui.components.ShopOSTopBar
import com.example.shopos.ui.theme.*
import com.example.shopos.ui.viewmodel.ShopViewModel
import kotlinx.coroutines.launch

@Composable
fun BackupRestoreScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            ShopOSTopBar(
                title = "Backup & Restore",
                onBack = onBack
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
            item {
                ShopOSCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Export Offline Database",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ShopTextDark
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Generate a complete, versioned JSON backup of your products, customer ledgers, sales history, and business profile.",
                            style = MaterialTheme.typography.bodySmall.copy(color = ShopTextMedium)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        ShopOSButton(
                            text = "Export Backup Now",
                            icon = Icons.Outlined.CloudUpload,
                            onClick = {
                                coroutineScope.launch {
                                    val json = viewModel.repository.exportBackupJson()
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TITLE, "ShopOS_Backup_${System.currentTimeMillis()}.json")
                                        putExtra(Intent.EXTRA_TEXT, json)
                                        type = "application/json"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Save or Share Backup File"))
                                    statusMessage = "Backup created successfully!"
                                }
                            }
                        )
                    }
                }
            }

            item {
                ShopOSCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Restore Database",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ShopTextDark
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Restore store data from a previously exported ShopOS JSON backup file.",
                            style = MaterialTheme.typography.bodySmall.copy(color = ShopTextMedium)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        ShopOSButton(
                            text = "Import Backup Data",
                            icon = Icons.Outlined.CloudDownload,
                            isPrimary = false,
                            onClick = { showRestoreDialog = true }
                        )
                    }
                }
            }

            statusMessage?.let { msg ->
                item {
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = ShopSuccessGreen,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        if (showRestoreDialog) {
            AlertDialog(
                onDismissRequest = { showRestoreDialog = false },
                title = { Text("Restore Backup") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Paste your ShopOS JSON backup text below to restore your store profile and data:",
                            fontSize = 13.sp,
                            color = ShopTextMedium
                        )
                        OutlinedTextField(
                            value = restoreJsonInput,
                            onValueChange = { restoreJsonInput = it },
                            placeholder = { Text("{\"version\": 1, \"app\": \"ShopOS\" ...}") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val success = viewModel.repository.restoreBackupJson(restoreJsonInput.trim())
                                if (success) {
                                    Toast.makeText(context, "Database restored successfully!", Toast.LENGTH_LONG).show()
                                    showRestoreDialog = false
                                    statusMessage = "Database restored from backup!"
                                } else {
                                    Toast.makeText(context, "Invalid ShopOS backup JSON format", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryLavender)
                    ) {
                        Text("Restore")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestoreDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun InfoContentScreen(
    title: String,
    content: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            ShopOSTopBar(
                title = title,
                onBack = onBack
            )
        },
        containerColor = ShopPearlBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                ShopOSCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = ShopTextDark,
                            lineHeight = 22.sp
                        ),
                        modifier = Modifier.padding(18.dp)
                    )
                }
            }
        }
    }
}

object LegalText {
    const val PRIVACY_POLICY = """
ShopOS Privacy Policy
Last Updated: October 2026

1. Offline-First Data Architecture
ShopOS is built from the ground up as a 100% offline-first application. All business information, catalog items, sales records, customer and supplier debts, cash flows, and operating expenses are stored exclusively on your local Android device using an encrypted local SQLite/Room database.

2. Zero Data Transmission
ShopOS does not transmit your store records, sales figures, phone contacts, or financial figures to any external server, third-party analytics provider, advertising network, or cloud database without your explicit action.

3. Device Permissions
- Storage / File Access: Only invoked when you explicitly choose to export or import a database backup or download reports.
- Camera: Only requested if you choose to scan physical barcodes on products using your device camera.

4. Data Portability & Safety
You retain complete ownership of all data. You may export your entire store history at any time through the Backup & Restore feature. Because ShopOS does not store copies on remote servers, we recommend storing your backup files securely in Google Drive or offline media.
"""

    const val TERMS_OF_SERVICE = """
ShopOS Terms of Service
Last Updated: October 2026

1. Acceptance of Terms
By installing and using ShopOS, you agree to these Terms. ShopOS is provided to assist shop owners and retailers with inventory tracking, sales recording, and ledger management.

2. Financial & Business Record Keeping
ShopOS provides calculations for gross sales, net profit, cost of goods sold (COGS), and customer balances based on data entered by the user. While ShopOS adheres to standard double-entry principles, you remain responsible for verifying calculations against physical cash counts and tax statutory regulations in your jurisdiction.

3. Data Responsibility
Because ShopOS operates locally without mandatory accounts, you are solely responsible for creating regular backups using the in-app Backup & Restore function.

4. Free and Open Software Foundations
ShopOS uses permissive open-source libraries and Jetpack Compose.
"""

    const val HELP_AND_SUPPORT = """
ShopOS Help & Support Guide

1. Quick Sale Workflow:
- Tap 'New Sale' from the Home dashboard or bottom navigation.
- Tap '+' or search for an item by name or barcode.
- Adjust quantities with the [- / +] controls.
- Choose payment method (Cash, Card, Bkash, Nagad).
- Tap 'Complete Sale' to generate an itemized invoice and print or share.

2. Managing Stock & Inventory:
- Go to Products tab to view all inventory.
- Items below your alert threshold display an orange 'Low' badge.
- Tap any item to open the 'Adjust Stock' dialog to restock or record damage.

3. Customer & Supplier Dues:
- Tap Customers or Suppliers in More or Home Quick Actions.
- Tap any customer with outstanding due to collect payments.
- Cash movements automatically synchronize with your cashbook.

4. Need Assistance?
ShopOS is maintained locally for maximum speed and security. You can export diagnostic reports from Settings -> Backup & Restore.
"""
}
