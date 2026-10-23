package com.example.shopos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopos.ui.components.ShopOSButton
import com.example.shopos.ui.components.ShopOSTopBar
import com.example.shopos.ui.theme.*
import com.example.shopos.ui.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedUnit by remember { mutableStateOf("Piece") }
    var unitExpanded by remember { mutableStateOf(false) }
    var purchasePrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("10") }
    var lowStockThreshold by remember { mutableStateOf("5") }
    var barcode by remember { mutableStateOf("") }

    val categories = listOf("Food", "Beverages", "Personal Care", "Household", "General")
    val units = listOf("Piece", "kg", "litre", "packet", "box")

    val isFormValid = name.isNotBlank() &&
            (sellingPrice.toDoubleOrNull() != null && sellingPrice.toDouble() >= 0)

    Scaffold(
        topBar = {
            ShopOSTopBar(
                title = "Add Product",
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
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ShopSurfaceWhite)
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                ShopOSButton(
                    text = "Save Product",
                    enabled = isFormValid,
                    onClick = {
                        val cost = purchasePrice.toDoubleOrNull() ?: 0.0
                        val price = sellingPrice.toDoubleOrNull() ?: 0.0
                        val currentStock = stock.toDoubleOrNull() ?: 0.0
                        val low = lowStockThreshold.toDoubleOrNull() ?: 5.0

                        viewModel.saveProduct(
                            name = name.trim(),
                            category = selectedCategory,
                            unit = selectedUnit,
                            purchaseCost = cost,
                            sellingPrice = price,
                            stock = currentStock,
                            lowStock = low,
                            barcode = barcode.trim(),
                            onDone = onBack
                        )
                    }
                )
            }
        },
        containerColor = ShopPearlBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo Upload Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF3F2FA))
                    .border(1.dp, ShopCardBorder, RoundedCornerShape(16.dp))
                    .clickable { /* Select product photo */ },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Add Photo",
                            tint = ShopPrimaryLavender,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add Photo",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ShopTextMedium,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            // Product Name
            Column {
                Text(
                    text = "Product Name *",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = ShopTextDark
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g. Maggi Noodles", color = ShopTextMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ShopSurfaceWhite,
                        unfocusedContainerColor = ShopSurfaceWhite,
                        focusedBorderColor = ShopPrimaryLavender,
                        unfocusedBorderColor = ShopCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Category Dropdown
            Column {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = ShopTextDark
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ShopSurfaceWhite,
                            unfocusedContainerColor = ShopSurfaceWhite,
                            focusedBorderColor = ShopPrimaryLavender,
                            unfocusedBorderColor = ShopCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Unit Dropdown
            Column {
                Text(
                    text = "Unit",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = ShopTextDark
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = !unitExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedUnit,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ShopSurfaceWhite,
                            unfocusedContainerColor = ShopSurfaceWhite,
                            focusedBorderColor = ShopPrimaryLavender,
                            unfocusedBorderColor = ShopCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        units.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(u) },
                                onClick = {
                                    selectedUnit = u
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Purchase Price & Selling Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Purchase Price",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = ShopTextDark
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = purchasePrice,
                        onValueChange = { purchasePrice = it },
                        placeholder = { Text("৳ 35", color = ShopTextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ShopSurfaceWhite,
                            unfocusedContainerColor = ShopSurfaceWhite,
                            focusedBorderColor = ShopPrimaryLavender,
                            unfocusedBorderColor = ShopCardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Selling Price *",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = ShopTextDark
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = sellingPrice,
                        onValueChange = { sellingPrice = it },
                        placeholder = { Text("৳ 45", color = ShopTextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ShopSurfaceWhite,
                            unfocusedContainerColor = ShopSurfaceWhite,
                            focusedBorderColor = ShopPrimaryLavender,
                            unfocusedBorderColor = ShopCardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Opening Stock & Threshold
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Opening Stock",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = ShopTextDark
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ShopSurfaceWhite,
                            unfocusedContainerColor = ShopSurfaceWhite,
                            focusedBorderColor = ShopPrimaryLavender,
                            unfocusedBorderColor = ShopCardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Low Stock Alert",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = ShopTextDark
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = lowStockThreshold,
                        onValueChange = { lowStockThreshold = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ShopSurfaceWhite,
                            unfocusedContainerColor = ShopSurfaceWhite,
                            focusedBorderColor = ShopPrimaryLavender,
                            unfocusedBorderColor = ShopCardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Barcode (optional)
            Column {
                Text(
                    text = "Barcode (optional)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = ShopTextDark
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    placeholder = { Text("Scan or enter barcode", color = ShopTextMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ShopSurfaceWhite,
                        unfocusedContainerColor = ShopSurfaceWhite,
                        focusedBorderColor = ShopPrimaryLavender,
                        unfocusedBorderColor = ShopCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
