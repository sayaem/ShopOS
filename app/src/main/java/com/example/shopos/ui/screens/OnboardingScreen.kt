package com.example.shopos.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.shopos.ui.components.ShopOSButton
import com.example.shopos.ui.components.ShopOSCard
import com.example.shopos.ui.theme.*
import com.example.shopos.ui.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: ShopViewModel,
    onComplete: () -> Unit
) {
    var shopName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Grocery & Retail") }
    var selectedCurrency by remember { mutableStateOf("৳") }
    var selectedLanguage by remember { mutableStateOf("en") }
    var startingCash by remember { mutableStateOf("") }
    var loadSampleData by remember { mutableStateOf(false) }

    var typeExpanded by remember { mutableStateOf(false) }
    val businessTypes = listOf(
        "Grocery & Retail",
        "Clothing & Fashion",
        "Electronics & Mobile",
        "Cosmetics & Pharmacy",
        "Household & Hardware",
        "General Store"
    )

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ShopSurfaceWhite)
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                ShopOSButton(
                    text = if (loadSampleData) "Setup & Load Demo Store" else "Open My Shop",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = {
                        val cash = startingCash.toDoubleOrNull() ?: 0.0
                        viewModel.completeOnboarding(
                            shopName = shopName.trim(),
                            ownerName = ownerName.trim(),
                            businessType = selectedType,
                            currencySymbol = selectedCurrency,
                            language = selectedLanguage,
                            startingCash = cash,
                            loadSampleData = loadSampleData,
                            onComplete = onComplete
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header / Logo
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ShopPrimaryLavender),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_shopos_logo),
                        contentDescription = "ShopOS",
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Welcome to ShopOS",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ShopTextDark
                    )
                )
                Text(
                    text = "Setup your store in seconds. Works 100% offline.",
                    style = MaterialTheme.typography.bodySmall.copy(color = ShopTextMuted),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Shop Name
            Column {
                Text(
                    text = "Shop Name",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = ShopTextDark)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    placeholder = { Text("e.g. Dhaka Express Grocers", color = ShopTextMuted) },
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

            // Owner Name
            Column {
                Text(
                    text = "Owner Name (optional)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = ShopTextDark)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    placeholder = { Text("e.g. Rahim Uddin", color = ShopTextMuted) },
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

            // Store Type Dropdown
            Column {
                Text(
                    text = "Store Category",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = ShopTextDark)
                )
                Spacer(modifier = Modifier.height(6.dp))
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
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
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        businessTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Currency & Opening Cash
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Currency Symbol",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = ShopTextDark)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = selectedCurrency,
                        onValueChange = { selectedCurrency = it },
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
                        text = "Opening Cash (optional)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = ShopTextDark)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = startingCash,
                        onValueChange = { startingCash = it },
                        placeholder = { Text("0.00", color = ShopTextMuted) },
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

            // Language Selector
            Column {
                Text(
                    text = "App Language",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = ShopTextDark)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isEn = selectedLanguage == "en"
                    ShopOSCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = if (isEn) ShopPrimaryContainer else ShopSurfaceWhite,
                        borderColor = if (isEn) ShopPrimaryLavender else ShopCardBorder,
                        onClick = { selectedLanguage = "en" }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "English",
                                fontWeight = if (isEn) FontWeight.Bold else FontWeight.Medium,
                                color = if (isEn) ShopPrimaryLavender else ShopTextDark
                            )
                        }
                    }

                    val isBn = selectedLanguage == "bn"
                    ShopOSCard(
                        modifier = Modifier.weight(1f),
                        backgroundColor = if (isBn) ShopPrimaryContainer else ShopSurfaceWhite,
                        borderColor = if (isBn) ShopPrimaryLavender else ShopCardBorder,
                        onClick = { selectedLanguage = "bn" }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "বাংলা (Bangla)",
                                fontWeight = if (isBn) FontWeight.Bold else FontWeight.Medium,
                                color = if (isBn) ShopPrimaryLavender else ShopTextDark
                            )
                        }
                    }
                }
            }

            // Data Choice Option: Sample Data vs Fresh Store
            ShopOSCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { loadSampleData = !loadSampleData },
                backgroundColor = if (loadSampleData) Color(0xFFF3F1FD) else ShopSurfaceWhite
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (loadSampleData) ShopPrimaryLavender else Color(0xFFE5E7EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (loadSampleData) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Explore Sample Store (Demo Mode)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = ShopTextDark
                            )
                        )
                        Text(
                            text = "Preloads sample products, dues and sales for evaluation.",
                            style = MaterialTheme.typography.bodySmall.copy(color = ShopTextMuted, fontSize = 11.sp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
