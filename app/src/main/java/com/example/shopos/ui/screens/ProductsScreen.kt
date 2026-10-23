package com.example.shopos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopos.data.model.Product
import com.example.shopos.ui.components.*
import com.example.shopos.ui.theme.*
import com.example.shopos.ui.viewmodel.ShopViewModel
import com.example.shopos.util.Formatters

@Composable
fun ProductsScreen(
    viewModel: ShopViewModel,
    onNavigateToAddProduct: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val allProducts by viewModel.products.collectAsState()
    val searchQuery by viewModel.productSearchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val profile by viewModel.businessProfile.collectAsState()
    val currency = profile?.currencySymbol ?: "৳"

    var selectedProductForAdjustment by remember { mutableStateOf<Product?>(null) }

    val categories = listOf("All", "Food", "Beverages", "Personal Care")

    val filteredProducts = remember(allProducts, searchQuery, selectedCategory) {
        allProducts.filter { prod ->
            val matchesSearch = searchQuery.isBlank() ||
                    prod.name.contains(searchQuery, ignoreCase = true) ||
                    prod.sku.contains(searchQuery, ignoreCase = true) ||
                    prod.barcode.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || prod.category.equals(selectedCategory, ignoreCase = true)
            matchesSearch && matchesCategory
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
                title = "Products",
                onBack = onBack,
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = ShopTextDark
                        )
                    }
                }
            )

            // Search Bar
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                ShopOSSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.setProductSearchQuery(it) },
                    placeholder = "Search products..."
                )
            }

            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    ShopOSFilterChip(
                        label = cat,
                        selected = selectedCategory == cat,
                        onClick = { viewModel.setSelectedCategory(cat) }
                    )
                }
            }

            // Product List
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No products found",
                            style = MaterialTheme.typography.titleMedium.copy(color = ShopTextMedium)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap + to add your first product",
                            style = MaterialTheme.typography.bodySmall.copy(color = ShopTextMuted)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductItemRow(
                            product = product,
                            currency = currency,
                            onClick = { selectedProductForAdjustment = product }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onNavigateToAddProduct,
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
                contentDescription = "Add Product",
                modifier = Modifier.size(24.dp)
            )
        }

        // Stock Adjustment Dialog
        selectedProductForAdjustment?.let { product ->
            AdjustStockDialog(
                product = product,
                currency = currency,
                onDismiss = { selectedProductForAdjustment = null },
                onConfirm = { delta, reason, note ->
                    viewModel.adjustStock(product.id, delta, reason, note)
                    selectedProductForAdjustment = null
                },
                onDelete = {
                    viewModel.deleteProduct(product)
                    selectedProductForAdjustment = null
                }
            )
        }
    }
}

@Composable
fun ProductItemRow(
    product: Product,
    currency: String,
    onClick: () -> Unit
) {
    val isLowStock = product.currentStock <= product.lowStockThreshold

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
            // Product Icon / Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF2F1FA)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = product.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ShopPrimaryLavender
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = ShopTextDark
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Formatters.formatCurrency(product.sellingPrice, currency),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ShopTextDark
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Stock: ${product.currentStock.toInt()}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ShopTextMuted
                        )
                    )
                }
            }

            if (isLowStock) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ShopBlushContainer)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Low",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ShopBlushCoral,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
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

@Composable
fun AdjustStockDialog(
    product: Product,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var deltaText by remember { mutableStateOf("10") }
    var isAddition by remember { mutableStateOf(true) }
    var reason by remember { mutableStateOf("RESTOCK") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Product: ${product.name}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Current Stock: ${product.currentStock.toInt()} ${product.unit} • Selling Price: ${Formatters.formatCurrency(product.sellingPrice, currency)}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = ShopTextMedium)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isAddition = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAddition) ShopSuccessGreen else ShopPillBackground,
                            contentColor = if (isAddition) Color.White else ShopTextDark
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+ Add Stock")
                    }
                    Button(
                        onClick = { isAddition = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isAddition) ShopBlushCoral else ShopPillBackground,
                            contentColor = if (!isAddition) Color.White else ShopTextDark
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("- Deduct")
                    }
                }

                OutlinedTextField(
                    value = deltaText,
                    onValueChange = { deltaText = it },
                    label = { Text("Quantity") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Reference (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = ShopBlushCoral),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Delete Product", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = deltaText.toDoubleOrNull() ?: 0.0
                    val finalDelta = if (isAddition) amount else -amount
                    val finalReason = if (isAddition) "RESTOCK" else "MANUAL"
                    onConfirm(finalDelta, finalReason, note)
                }
            ) {
                Text("Confirm", fontWeight = FontWeight.Bold, color = ShopPrimaryLavender)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ShopTextMuted)
            }
        }
    )
}
