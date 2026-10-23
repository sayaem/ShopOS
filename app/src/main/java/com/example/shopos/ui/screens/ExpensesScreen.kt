package com.example.shopos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopos.data.model.Expense
import com.example.shopos.ui.components.ShopOSCard
import com.example.shopos.ui.components.ShopOSFilterChip
import com.example.shopos.ui.components.ShopOSTopBar
import com.example.shopos.ui.theme.*
import com.example.shopos.ui.viewmodel.ShopViewModel
import com.example.shopos.util.Formatters
import java.util.Calendar

@Composable
fun ExpensesScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit
) {
    val expenses by viewModel.expenses.collectAsState()
    val profile by viewModel.businessProfile.collectAsState()
    val currency = profile?.currencySymbol ?: "৳"

    var selectedFilter by remember { mutableStateOf("All") }
    var showAddExpenseDialog by remember { mutableStateOf(false) }

    val filterOptions = listOf("All", "Daily", "Monthly")

    val todayStartMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val monthStartMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val filteredExpenses = remember(expenses, selectedFilter, todayStartMillis, monthStartMillis) {
        when (selectedFilter) {
            "Daily" -> expenses.filter { it.dateMillis >= todayStartMillis }
            "Monthly" -> expenses.filter { it.dateMillis >= monthStartMillis }
            else -> expenses
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ShopPearlBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            ShopOSTopBar(
                title = "Expenses",
                onBack = onBack,
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = ShopTextDark
                        )
                    }
                }
            )

            // Time filters
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOptions) { filter ->
                    ShopOSFilterChip(
                        label = filter,
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter }
                    )
                }
            }

            if (filteredExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.ReceiptLong,
                            contentDescription = null,
                            tint = ShopTextMuted,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No expenses recorded",
                            style = MaterialTheme.typography.titleMedium.copy(color = ShopTextMedium)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap + below to record a business expense",
                            style = MaterialTheme.typography.bodySmall.copy(color = ShopTextMuted)
                        )
                    }
                }
            } else {
                // Expenses List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredExpenses, key = { it.id }) { expense ->
                        ExpenseItemRow(
                            expense = expense,
                            currency = currency,
                            onDelete = { viewModel.deleteExpense(expense) }
                        )
                    }
                }
            }
        }

        // FAB + to add expense
        FloatingActionButton(
            onClick = { showAddExpenseDialog = true },
            containerColor = ShopPrimaryDark,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
                .size(54.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Expense",
                modifier = Modifier.size(24.dp)
            )
        }

        if (showAddExpenseDialog) {
            AddExpenseDialog(
                onDismiss = { showAddExpenseDialog = false },
                onAdd = { title, category, amount, method ->
                    viewModel.addExpense(title, category, amount, method)
                    showAddExpenseDialog = false
                }
            )
        }
    }
}

@Composable
fun ExpenseItemRow(
    expense: Expense,
    currency: String,
    onDelete: () -> Unit
) {
    val icon = when (expense.category.lowercase()) {
        "rent" -> Icons.Outlined.CalendarMonth
        "electricity" -> Icons.Outlined.ElectricBolt
        "transport" -> Icons.Outlined.DirectionsCar
        "salary" -> Icons.Outlined.Person
        else -> Icons.Outlined.ShoppingBag
    }

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
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F2F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = ShopTextMedium,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = expense.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = ShopTextDark
                        )
                    )
                    Text(
                        text = "${Formatters.formatDateShort(expense.dateMillis)} • ${expense.paymentMethod}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ShopTextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = Formatters.formatCurrency(expense.amount, currency),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ShopTextDark
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = ShopTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Double, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Rent") }
    var amountText by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("Cash") }

    val categories = listOf("Rent", "Electricity", "Transport", "Salary", "Miscellaneous")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Category", fontSize = 12.sp, color = ShopTextMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        Button(
                            onClick = { category = cat },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (category == cat) ShopPrimaryLavender else ShopPillBackground,
                                contentColor = if (category == cat) Color.White else ShopTextDark
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(cat, fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0) {
                        onAdd(title.trim(), category, amt, method)
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
