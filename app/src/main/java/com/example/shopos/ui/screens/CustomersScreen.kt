package com.example.shopos.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
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
import com.example.shopos.data.model.Customer
import com.example.shopos.ui.components.ShopOSButton
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
    val context = LocalContext.current
    val customers by viewModel.customers.collectAsState()
    val profile by viewModel.businessProfile.collectAsState()
    val currency = profile?.currencySymbol ?: "৳"

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCustomerForDetails by remember { mutableStateOf<Customer?>(null) }
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
                        onClick = { selectedCustomerForDetails = customer }
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
                    Toast.makeText(context, "Customer $name saved", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Customer Details & Quick Actions Dialog
        selectedCustomerForDetails?.let { customer ->
            CustomerDetailDialog(
                customer = customer,
                currency = currency,
                onDismiss = { selectedCustomerForDetails = null },
                onCollectDue = {
                    selectedCustomerForDetails = null
                    selectedCustomerForDue = customer
                },
                onCall = {
                    if (customer.phone.isNotBlank()) {
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${customer.phone}")
                        }
                        context.startActivity(dialIntent)
                    } else {
                        Toast.makeText(context, "No phone number saved for this customer", Toast.LENGTH_SHORT).show()
                    }
                },
                onSendSms = {
                    if (customer.phone.isNotBlank()) {
                        val shopName = profile?.shopName ?: "Our Store"
                        val dueStr = Formatters.formatCurrency(customer.totalDue, currency)
                        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("smsto:${customer.phone}")
                            putExtra("sms_body", "Dear ${customer.name}, your outstanding due balance at $shopName is $dueStr. Thank you.")
                        }
                        context.startActivity(smsIntent)
                    } else {
                        Toast.makeText(context, "No phone number saved for this customer", Toast.LENGTH_SHORT).show()
                    }
                },
                onDelete = {
                    viewModel.deleteCustomer(customer)
                    selectedCustomerForDetails = null
                    Toast.makeText(context, "Customer ${customer.name} deleted", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(context, "Collected ${Formatters.formatCurrency(amount, currency)} from ${customer.name}", Toast.LENGTH_SHORT).show()
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
                        if (customer.phone.isNotBlank()) "${customer.phone} • Due Outstanding" else "Outstanding Due"
                    } else {
                        if (customer.phone.isNotBlank()) customer.phone else "No due"
                    },
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
fun CustomerDetailDialog(
    customer: Customer,
    currency: String,
    onDismiss: () -> Unit,
    onCollectDue: () -> Unit,
    onCall: () -> Unit,
    onSendSms: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEDE9FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = customer.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = ShopPrimaryLavender,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = customer.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    if (customer.phone.isNotBlank()) {
                        Text(text = customer.phone, style = MaterialTheme.typography.bodySmall.copy(color = ShopTextMuted))
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Balance card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (customer.totalDue > 0) Color(0xFFFFF1F2) else Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Outstanding Due:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = Formatters.formatCurrency(customer.totalDue, currency),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (customer.totalDue > 0) ShopBlushCoral else ShopSuccessGreen
                            )
                        )
                    }
                }

                if (customer.address.isNotBlank()) {
                    Text(
                        text = "Address: ${customer.address}",
                        style = MaterialTheme.typography.bodySmall.copy(color = ShopTextMedium)
                    )
                }

                // Quick Communication Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCall,
                        enabled = customer.phone.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Outlined.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onSendSms,
                        enabled = customer.phone.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Outlined.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SMS", fontSize = 12.sp)
                    }
                }

                // Collect Due Action
                if (customer.totalDue > 0) {
                    ShopOSButton(
                        text = "Collect Due Payment",
                        icon = Icons.Outlined.Payments,
                        onClick = onCollectDue
                    )
                }

                // Delete customer option (if not default)
                if (customer.id != 1L) {
                    TextButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        colors = ButtonDefaults.textButtonColors(contentColor = ShopBlushCoral)
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete Customer", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Customer?") },
            text = { Text("Are you sure you want to delete ${customer.name}? Their past sales records will be preserved.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShopBlushCoral)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
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
