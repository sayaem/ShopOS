package com.example.shopos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.shopos.data.model.Product
import com.example.shopos.data.repository.ShopRepository
import com.example.shopos.ui.components.*
import com.example.shopos.ui.theme.*
import com.example.shopos.ui.viewmodel.ShopViewModel
import com.example.shopos.util.Formatters

@Composable
fun NewSaleScreen(
    viewModel: ShopViewModel,
    onSaleCompleted: (Long) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val allProducts by viewModel.products.collectAsState()
    val profile by viewModel.businessProfile.collectAsState()
    val selectedPaymentMethod by viewModel.cartPaymentMethod.collectAsState()
    val discount by viewModel.cartDiscount.collectAsState()
    val currency = profile?.currencySymbol ?: "৳"

    var searchQuery by remember { mutableStateOf("") }
    var showProductPicker by remember { mutableStateOf(false) }

    val subtotal = cartItems.sumOf { it.lineTotal }
    val total = (subtotal - discount).coerceAtLeast(0.0)

    val paymentMethods = listOf("Cash", "Card", "Bkash", "Nagad")

    val searchResults = remember(searchQuery, allProducts) {
        if (searchQuery.isBlank()) emptyList()
        else allProducts.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.barcode.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            ShopOSTopBar(
                title = "New Sale",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showProductPicker = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Product to Cart",
                            tint = ShopTextDark
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ShopSurfaceWhite)
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                // Summary Block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Subtotal",
                        style = MaterialTheme.typography.bodyMedium.copy(color = ShopTextMedium)
                    )
                    Text(
                        text = Formatters.formatCurrency(subtotal, currency),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = ShopTextDark
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Discount",
                        style = MaterialTheme.typography.bodyMedium.copy(color = ShopTextMedium)
                    )
                    Text(
                        text = Formatters.formatCurrency(discount, currency),
                        style = MaterialTheme.typography.bodyMedium.copy(color = ShopTextMedium)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = ShopCardBorder)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ShopTextDark
                        )
                    )
                    Text(
                        text = Formatters.formatCurrency(total, currency),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ShopTextDark,
                            fontSize = 20.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Methods
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    paymentMethods.forEach { method ->
                        val isSelected = selectedPaymentMethod == method
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ShopPrimaryContainer else ShopPillBackground)
                                .border(
                                    1.dp,
                                    if (isSelected) ShopPrimaryLavender else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.setCartPaymentMethod(method) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = method,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) ShopPrimaryLavender else ShopTextMedium
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        viewModel.completeSale { saleId ->
                            onSaleCompleted(saleId)
                        }
                    },
                    enabled = cartItems.isNotEmpty(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ShopPrimaryLavender,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Complete Sale",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        containerColor = ShopPearlBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search or barcode bar
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                ShopOSSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search product / barcode..."
                )
            }

            // Quick live search results popup
            if (searchResults.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = ShopSurfaceWhite,
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        searchResults.take(3).forEach { prod ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addToCart(prod)
                                        searchQuery = ""
                                    }
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = prod.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "+ Add ${Formatters.formatCurrency(prod.sellingPrice, currency)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ShopPrimaryLavender,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Cart Items List
            if (cartItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = null,
                            tint = ShopTextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Your cart is empty",
                            style = MaterialTheme.typography.titleMedium.copy(color = ShopTextMedium)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Search or select items to start a sale",
                            style = MaterialTheme.typography.bodySmall.copy(color = ShopTextMuted)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showProductPicker = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ShopPrimaryContainer, contentColor = ShopPrimaryLavender),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Browse Catalog")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(cartItems, key = { it.product.id }) { item ->
                        CartItemRow(
                            item = item,
                            currency = currency,
                            onIncrement = { viewModel.updateCartQuantity(item.product.id, 1.0) },
                            onDecrement = { viewModel.updateCartQuantity(item.product.id, -1.0) },
                            onRemove = { viewModel.removeFromCart(item.product.id) }
                        )
                    }
                }
            }
        }

        // Product Picker Bottom Sheet / Dialog
        if (showProductPicker) {
            ProductPickerDialog(
                products = allProducts,
                currency = currency,
                onDismiss = { showProductPicker = false },
                onSelect = { prod ->
                    viewModel.addToCart(prod)
                }
            )
        }
    }
}

@Composable
fun CartItemRow(
    item: ShopRepository.CartItem,
    currency: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    ShopOSCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF2F1FA)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.product.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ShopPrimaryLavender
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = ShopTextDark
                    ),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = Formatters.formatCurrency(item.product.sellingPrice, currency),
                    style = MaterialTheme.typography.bodySmall.copy(color = ShopTextMuted)
                )
            }

            // Stepper [- qty +]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF1F2F6))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                IconButton(
                    onClick = onDecrement,
                    modifier = Modifier.size(28.dp)
                ) {
                    Text(
                        text = "-",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopTextDark
                    )
                }
                Text(
                    text = item.quantity.toInt().toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ShopTextDark
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                IconButton(
                    onClick = onIncrement,
                    modifier = Modifier.size(28.dp)
                ) {
                    Text(
                        text = "+",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopTextDark
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = Formatters.formatCurrency(item.lineTotal, currency),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ShopTextDark
                )
            )
        }
    }
}

@Composable
fun ProductPickerDialog(
    products: List<Product>,
    currency: String,
    onDismiss: () -> Unit,
    onSelect: (Product) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Product") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products) { prod ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(prod) }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(prod.name, fontWeight = FontWeight.SemiBold)
                            Text("Stock: ${prod.currentStock.toInt()}", fontSize = 12.sp, color = ShopTextMuted)
                        }
                        Text(Formatters.formatCurrency(prod.sellingPrice, currency), fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
