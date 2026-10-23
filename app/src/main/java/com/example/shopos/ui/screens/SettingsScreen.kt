package com.example.shopos.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopos.ui.components.ShopOSCard
import com.example.shopos.ui.components.ShopOSTopBar
import com.example.shopos.ui.theme.*
import com.example.shopos.ui.viewmodel.ShopViewModel

@Composable
fun SettingsScreen(
    viewModel: ShopViewModel,
    onNavigateToRoute: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.businessProfile.collectAsState()
    val ownerName = profile?.ownerName ?: "Shop Owner"
    val email = profile?.email ?: "owner@shopos.local"
    val language = profile?.language ?: "en"

    var showProfileDialog by remember { mutableStateOf(false) }
    var showResetDemoDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ShopOSTopBar(
                title = "Settings",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile Card
            item {
                ShopOSCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showProfileDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0E7FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ownerName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = ShopPrimaryLavender,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ownerName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ShopTextDark
                                )
                            )
                            Text(
                                text = profile?.shopName ?: "ShopOS Store",
                                style = MaterialTheme.typography.bodySmall.copy(color = ShopTextMuted)
                            )
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

            // Options List
            item {
                ShopOSCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        SettingsRow(
                            icon = Icons.Outlined.Storefront,
                            title = "Business Profile",
                            subtitle = profile?.shopName ?: "ShopOS Store",
                            onClick = { showProfileDialog = true }
                        )
                        HorizontalDivider(color = ShopCardBorder, modifier = Modifier.padding(start = 56.dp))
                        SettingsRow(
                            icon = Icons.Outlined.Language,
                            title = "Language",
                            subtitle = if (language == "bn") "বাংলা (Bangla)" else "English (US)",
                            onClick = {
                                val next = if (language == "bn") "en" else "bn"
                                viewModel.setLanguage(next)
                                Toast.makeText(context, "Language switched to ${if (next == "bn") "Bangla" else "English"}", Toast.LENGTH_SHORT).show()
                            }
                        )
                        HorizontalDivider(color = ShopCardBorder, modifier = Modifier.padding(start = 56.dp))
                        SettingsRow(
                            icon = Icons.Outlined.CloudUpload,
                            title = "Backup & Restore",
                            subtitle = "Export or import JSON offline backup",
                            onClick = { onNavigateToRoute("backup_restore") }
                        )
                        HorizontalDivider(color = ShopCardBorder, modifier = Modifier.padding(start = 56.dp))
                        SettingsRow(
                            icon = Icons.Outlined.Palette,
                            title = "Appearance",
                            subtitle = "Light / Pearl Lavender",
                            onClick = {
                                Toast.makeText(context, "Light / Pearl theme is active", Toast.LENGTH_SHORT).show()
                            }
                        )
                        HorizontalDivider(color = ShopCardBorder, modifier = Modifier.padding(start = 56.dp))
                        SettingsRow(
                            icon = Icons.Outlined.Notifications,
                            title = "Stock & Due Alerts",
                            subtitle = "Automatic low stock badges enabled",
                            onClick = {
                                Toast.makeText(context, "Low stock threshold alert system is active", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // Data Management Section
            item {
                Text(
                    text = "Data Management",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ShopTextMedium
                    ),
                    modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                )
            }

            // Fresh Start Option (Clear data)
            item {
                ShopOSCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showClearDataDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFFF1F2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteSweep,
                                contentDescription = null,
                                tint = ShopBlushCoral,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Start Fresh (Clear Store Data)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = ShopTextDark
                                )
                            )
                            Text(
                                text = "Clear sample items, sales, and dues for real store usage",
                                style = MaterialTheme.typography.bodySmall.copy(color = ShopTextMuted)
                            )
                        }
                    }
                }
            }

            // Demo Data Reset
            item {
                ShopOSCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showResetDemoDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ShopPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = null,
                                tint = ShopPrimaryLavender,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Load Sample Retail Data",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = ShopTextDark
                                )
                            )
                            Text(
                                text = "Explore features with sample grocery products and ledger entries",
                                style = MaterialTheme.typography.bodySmall.copy(color = ShopTextMuted)
                            )
                        }
                    }
                }
            }
        }

        // Profile Dialog
        if (showProfileDialog) {
            EditProfileDialog(
                currentName = profile?.shopName ?: "ShopOS Store",
                currentOwner = ownerName,
                currentPhone = profile?.phone ?: "+880 1712-345678",
                currentAddress = profile?.address ?: "Gulshan-1, Dhaka, Bangladesh",
                currentCurrency = profile?.currencySymbol ?: "৳",
                onDismiss = { showProfileDialog = false },
                onSave = { name, owner, phone, addr, curr ->
                    viewModel.updateProfile(name, owner, phone, addr, curr, language)
                    showProfileDialog = false
                    Toast.makeText(context, "Business profile saved", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Confirm Reset Demo Data
        if (showResetDemoDialog) {
            AlertDialog(
                onDismissRequest = { showResetDemoDialog = false },
                title = { Text("Load Sample Retail Data?") },
                text = { Text("This will load realistic demo products, customers, suppliers, and transactions so you can explore all features.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetDemoData()
                            showResetDemoDialog = false
                            Toast.makeText(context, "Sample retail dataset loaded!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryLavender)
                    ) {
                        Text("Load Sample Data")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDemoDialog = false }) { Text("Cancel") }
                }
            )
        }

        // Confirm Clear Store Data
        if (showClearDataDialog) {
            AlertDialog(
                onDismissRequest = { showClearDataDialog = false },
                title = { Text("Start Fresh?") },
                text = { Text("This will remove all products, transactions, and ledger entries, leaving an empty store ready for your real business data.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearToEmpty()
                            showClearDataDialog = false
                            Toast.makeText(context, "Store cleared. Ready for your products!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ShopBlushCoral)
                    ) {
                        Text("Start Fresh")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDataDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF1F2F6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ShopTextMedium,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = ShopTextDark
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = ShopTextMuted)
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = ShopTextMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun EditProfileDialog(
    currentName: String,
    currentOwner: String,
    currentPhone: String,
    currentAddress: String,
    currentCurrency: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var owner by remember { mutableStateOf(currentOwner) }
    var phone by remember { mutableStateOf(currentPhone) }
    var address by remember { mutableStateOf(currentAddress) }
    var currency by remember { mutableStateOf(currentCurrency) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Business Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Shop Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = owner,
                    onValueChange = { owner = it },
                    label = { Text("Owner Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Shop Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it },
                    label = { Text("Currency Symbol (e.g. ৳ or $)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, owner, phone, address, currency) },
                colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryLavender)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
