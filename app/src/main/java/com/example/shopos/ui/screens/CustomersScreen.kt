package com.example.shopos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopos.data.model.Customer
import com.example.shopos.ui.components.ShopOSCard
import com.example.shopos.ui.components.ShopOSSearchBar
import com.example.shopos.ui.components.ShopOSTopBar
import com.example.shopos.ui.theme.*
import com.example.shopos.ui.viewmodel.ShopViewModel
import com.example.shopos.util.Formatters

@Composable
fun CustomersScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit
) {
    val customers by viewModel.customers.collectAsState()
    val profile by viewModel.businessProfile.collectAsState()
    val currency = profile?.currencySymbol ?: "৳"

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCustomerForDue by remember { mutableStateOf<Customer?>(null) }

    val filteredCustomers = remember(customers, searchQuery) {
        if (searchQuery.isBlank()) customers
        else customers.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
        }
    }

    Scaffold(
        topBar = {
            ShopOSTopBar(
                title = "Customers",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Customer",
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
                    placeholder = "Search customer..."
                )
            }

            // Customer List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredCustomers, key = { it.id }) { customer ->
                    CustomerItemRow(
                        customer = customer,
                        currency = currency,
                        onClick = { selectedCustomerForDue = customer }
                    )
                }
            }
        }

        // Add Customer Dialog
        if (showAddDialog) {
            AddCustomerDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, phone, address, initialDue ->
                    viewModel.addCustomer(name, phone, address, initialDue)
                    showAddDialog = false
                }
            )
        }

        // Collect Due Dialog
        selectedCustomerForDue?.let { customer ->
            CollectDueDialog(
                customer = customer,
                currency = currency,
                onDismiss = { selectedCustomerForDue = null },
                onCollect = { amount, method, note ->
                    viewModel.collectDue(customer.id, amount, method, note)
                    selectedCustomerForDue = null
                }
            )
        }
    }
}

@Composable
fun CustomerItemRow(
    customer: Customer,
    currency: String,
    onClick: () -> Unit
) {
    val hasDue = customer.totalDue > 0

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
                    .background(if (customer.id == 1L) Color(0xFFF1F2F6) else Color(0xFFEDE9FE)),
                contentAlignment = Alignment.Center
            ) {
                if (customer.id == 1L) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = ShopTextMedium,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = customer.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = ShopPrimaryLavender,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = ShopTextDark
                    )
                )
                Text(
                    text = if (hasDue) {
                        if (customer.notes.isNotBlank()) "Due • ${customer.notes}" else "Outstanding Due"
                    } else "No due",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (hasDue) ShopBlushCoral else ShopTextMuted,
                        fontSize = 11.sp,
                        fontWeight = if (hasDue) FontWeight.SemiBold else FontWeight.Normal
                    )
                )
            }

            Text(
                text = Formatters.formatCurrency(customer.totalDue, currency),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (hasDue) ShopBlushCoral else ShopTextDark
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
fun CollectDueDialog(
    customer: Customer,
    currency: String,
    onDismiss: () -> Unit,
    onCollect: (Double, String, String) -> Unit
) {
    var amountText by remember { mutableStateOf(customer.totalDue.toString()) }
    var selectedMethod by remember { mutableStateOf("Cash") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Collect Due: ${customer.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Total Outstanding: ${Formatters.formatCurrency(customer.totalDue, currency)}",
                    fontWeight = FontWeight.Bold,
                    color = ShopBlushCoral
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount to Collect") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Cash", "Bkash", "Nagad").forEach { method ->
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
                    label = { Text("Note (optional)") },
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
                        onCollect(amt, selectedMethod, note)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryLavender)
            ) {
                Text("Collect Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var initialDue by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Customer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = initialDue,
                    onValueChange = { initialDue = it },
                    label = { Text("Initial Due (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val due = initialDue.toDoubleOrNull() ?: 0.0
                        onAdd(name.trim(), phone.trim(), address.trim(), due)
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
