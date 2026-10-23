package com.example.shopos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopos.data.model.Supplier
import com.example.shopos.ui.components.ShopOSCard
import com.example.shopos.ui.components.ShopOSSearchBar
import com.example.shopos.ui.components.ShopOSTopBar
import com.example.shopos.ui.theme.*
import com.example.shopos.ui.viewmodel.ShopViewModel
import com.example.shopos.util.Formatters

@Composable
fun SuppliersScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit
) {
    val suppliers by viewModel.suppliers.collectAsState()
    val profile by viewModel.businessProfile.collectAsState()
    val currency = profile?.currencySymbol ?: "৳"

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedSupplierForPayment by remember { mutableStateOf<Supplier?>(null) }

    val filteredSuppliers = remember(suppliers, searchQuery) {
        if (searchQuery.isBlank()) suppliers
        else suppliers.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.contactPerson.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            ShopOSTopBar(
                title = "Suppliers",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Supplier",
                            tint = ShopTextDark
                        )
                    }
                }
            )
        },
        containerColor = ShopPearlBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                ShopOSSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search supplier..."
                )
            }

            // Suppliers List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSuppliers, key = { it.id }) { supplier ->
                    SupplierItemRow(
                        supplier = supplier,
                        currency = currency,
                        onClick = { selectedSupplierForPayment = supplier }
                    )
                }
            }
        }

        // Add Supplier Dialog
        if (showAddDialog) {
            AddSupplierDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, phone, contactPerson, initialPayable ->
                    viewModel.addSupplier(name, phone, contactPerson, initialPayable)
                    showAddDialog = false
                }
            )
        }

        // Pay Supplier Dialog
        selectedSupplierForPayment?.let { supplier ->
            PaySupplierDialog(
                supplier = supplier,
                currency = currency,
                onDismiss = { selectedSupplierForPayment = null },
                onPay = { amount, method, note ->
                    viewModel.paySupplier(supplier.id, amount, method, note)
                    selectedSupplierForPayment = null
                }
            )
        }
    }
}

@Composable
fun SupplierItemRow(
    supplier: Supplier,
    currency: String,
    onClick: () -> Unit
) {
    val hasPayable = supplier.totalPayable > 0

    ShopOSCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0F2FE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Storefront,
                    contentDescription = null,
                    tint = ShopInfoBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = supplier.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = ShopTextDark
                    )
                )
                Text(
                    text = if (hasPayable) {
                        if (supplier.notes.isNotBlank()) "Due • ${supplier.notes}" else "Outstanding Payable"
                    } else "No due",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (hasPayable) ShopBlushCoral else ShopTextMuted,
                        fontSize = 11.sp,
                        fontWeight = if (hasPayable) FontWeight.SemiBold else FontWeight.Normal
                    )
                )
            }

            Text(
                text = Formatters.formatCurrency(supplier.totalPayable, currency),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (hasPayable) ShopBlushCoral else ShopTextDark
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = ShopTextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun PaySupplierDialog(
    supplier: Supplier,
    currency: String,
    onDismiss: () -> Unit,
    onPay: (Double, String, String) -> Unit
) {
    var amountText by remember { mutableStateOf(supplier.totalPayable.toString()) }
    var selectedMethod by remember { mutableStateOf("Cash") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pay Supplier: ${supplier.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Total Payable: ${Formatters.formatCurrency(supplier.totalPayable, currency)}",
                    fontWeight = FontWeight.Bold,
                    color = ShopBlushCoral
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Amount") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Cash", "Bank", "Bkash").forEach { method ->
                        val isSel = selectedMethod == method
                        Button(
                            onClick = { selectedMethod = method },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) ShopPrimaryLavender else ShopPillBackground,
                                contentColor = if (isSel) Color.White else ShopTextDark
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(method, fontSize = 12.sp)
                        }
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Reference / Note") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onPay(amt, selectedMethod, note)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryLavender)
            ) {
                Text("Confirm Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddSupplierDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var initialPayable by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Supplier") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Supplier Company *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contactPerson,
                    onValueChange = { contactPerson = it },
                    label = { Text("Contact Person") },
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
                    value = initialPayable,
                    onValueChange = { initialPayable = it },
                    label = { Text("Initial Payable (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val payable = initialPayable.toDoubleOrNull() ?: 0.0
                        onAdd(name.trim(), phone.trim(), contactPerson.trim(), payable)
                    }
                },
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
