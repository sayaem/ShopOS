package com.example.shopos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shopos.data.model.Product
import com.example.shopos.ui.components.ShopOSFilterChip
import com.example.shopos.ui.components.ShopOSSearchBar
import com.example.shopos.ui.components.ShopOSTopBar
import com.example.shopos.ui.theme.ShopPearlBackground
import com.example.shopos.ui.theme.ShopPrimaryLavender
import com.example.shopos.ui.theme.ShopTextDark
import com.example.shopos.ui.viewmodel.ShopViewModel

@Composable
fun InventoryScreen(
    viewModel: ShopViewModel,
    onNavigateToAddProduct: () -> Unit,
    onBack: () -> Unit
) {
    val allProducts by viewModel.products.collectAsState()
    val profile by viewModel.businessProfile.collectAsState()
    val currency = profile?.currencySymbol ?: "৳"

    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedProductForAdjustment by remember { mutableStateOf<Product?>(null) }

    val filterOptions = listOf("All", "In Stock", "Low Stock", "Out of Stock")

    val filteredList = remember(allProducts, selectedFilter, searchQuery) {
        val bySearch = if (searchQuery.isBlank()) allProducts else {
            allProducts.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.barcode.contains(searchQuery, ignoreCase = true)
            }
        }
        when (selectedFilter) {
            "In Stock" -> bySearch.filter { it.currentStock > it.lowStockThreshold }
            "Low Stock" -> bySearch.filter { it.currentStock in 1.0..it.lowStockThreshold }
            "Out of Stock" -> bySearch.filter { it.currentStock <= 0 }
            else -> bySearch
        }
    }

    Scaffold(
        topBar = {
            ShopOSTopBar(
                title = "Inventory",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { isSearchVisible = !isSearchVisible }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (isSearchVisible) ShopPrimaryLavender else ShopTextDark
                        )
                    }
                    IconButton(onClick = onNavigateToAddProduct) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Product",
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
            if (isSearchVisible) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    ShopOSSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search inventory items..."
                    )
                }
            }
            // Filter Chips
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

            // Products list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { product ->
                    ProductItemRow(
                        product = product,
                        currency = currency,
                        onClick = { selectedProductForAdjustment = product }
                    )
                }
            }
        }

        // Adjust Stock Dialog
        selectedProductForAdjustment?.let { product ->
            AdjustStockDialog(
                product = product,
                currency = currency,
                onDismiss = { selectedProductForAdjustment = null },
                onConfirm = { delta, reason, note ->
                    viewModel.adjustStock(product.id, delta, reason, note)
                    selectedProductForAdjustment = null
                }
            )
        }
    }
}
