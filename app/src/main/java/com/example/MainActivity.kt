package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.shopos.ui.components.ShopOSBottomNav
import com.example.shopos.ui.screens.*
import com.example.shopos.ui.theme.ShopOSTheme
import com.example.shopos.ui.theme.ShopPearlBackground
import com.example.shopos.ui.viewmodel.ShopViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ShopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShopOSTheme {
                ShopOSApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ShopOSApp(viewModel: ShopViewModel) {
    // Current navigation route
    var currentRoute by remember { mutableStateOf("home") }
    var selectedReceiptSaleId by remember { mutableStateOf<Long?>(null) }

    // Navigation back stack tracking
    val backStack = remember { mutableStateListOf("home") }

    fun navigateTo(route: String) {
        if (currentRoute != route) {
            backStack.add(route)
            currentRoute = route
        }
    }

    fun popBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
            currentRoute = backStack.last()
        }
    }

    val primaryTabs = listOf("home", "products", "sales", "reports", "more")
    val isPrimaryTab = currentRoute in primaryTabs

    BackHandler(enabled = backStack.size > 1) {
        popBack()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ShopPearlBackground),
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            if (isPrimaryTab) {
                ShopOSBottomNav(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (currentRoute != route) {
                            backStack.clear()
                            backStack.add(route)
                            currentRoute = route
                        }
                    }
                )
            }
        },
        containerColor = ShopPearlBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRoute) {
                "splash" -> SplashScreen(
                    onContinue = {
                        backStack.clear()
                        backStack.add("home")
                        currentRoute = "home"
                    }
                )

                "home" -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToRoute = { route -> navigateTo(route) }
                )

                "products" -> ProductsScreen(
                    viewModel = viewModel,
                    onNavigateToAddProduct = { navigateTo("add_product") },
                    onBack = if (!isPrimaryTab) ({ popBack() }) else null
                )

                "add_product" -> AddEditProductScreen(
                    viewModel = viewModel,
                    onBack = { popBack() }
                )

                "sales" -> NewSaleScreen(
                    viewModel = viewModel,
                    onSaleCompleted = { saleId ->
                        selectedReceiptSaleId = saleId
                        navigateTo("receipt")
                    },
                    onBack = if (!isPrimaryTab) ({ popBack() }) else null
                )

                "receipt" -> ReceiptScreen(
                    saleId = selectedReceiptSaleId ?: 1L,
                    viewModel = viewModel,
                    onDone = {
                        backStack.clear()
                        backStack.add("home")
                        currentRoute = "home"
                    }
                )

                "inventory" -> InventoryScreen(
                    viewModel = viewModel,
                    onNavigateToAddProduct = { navigateTo("add_product") },
                    onBack = { popBack() }
                )

                "customers" -> CustomersScreen(
                    viewModel = viewModel,
                    onBack = { popBack() }
                )

                "suppliers" -> SuppliersScreen(
                    viewModel = viewModel,
                    onBack = { popBack() }
                )

                "expenses" -> ExpensesScreen(
                    viewModel = viewModel,
                    onBack = { popBack() }
                )

                "reports" -> ReportsScreen(
                    viewModel = viewModel,
                    onNavigateToSalesReport = { navigateTo("sales_report") },
                    onNavigateToProfitLoss = { navigateTo("profit_loss") },
                    onBack = if (!isPrimaryTab) ({ popBack() }) else null
                )

                "sales_report" -> SalesReportScreen(
                    viewModel = viewModel,
                    onBack = { popBack() }
                )

                "profit_loss" -> ProfitLossScreen(
                    viewModel = viewModel,
                    onBack = { popBack() }
                )

                "more" -> MoreScreen(
                    onNavigateToRoute = { route -> navigateTo(route) }
                )

                "settings" -> SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToRoute = { route -> navigateTo(route) },
                    onBack = { popBack() }
                )

                "backup_restore" -> BackupRestoreScreen(
                    viewModel = viewModel,
                    onBack = { popBack() }
                )

                "privacy" -> InfoContentScreen(
                    title = "Privacy Policy",
                    content = LegalText.PRIVACY_POLICY,
                    onBack = { popBack() }
                )

                "terms" -> InfoContentScreen(
                    title = "Terms & Conditions",
                    content = LegalText.TERMS_OF_SERVICE,
                    onBack = { popBack() }
                )

                "help" -> InfoContentScreen(
                    title = "Help & Support",
                    content = LegalText.HELP_AND_SUPPORT,
                    onBack = { popBack() }
                )

                else -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToRoute = { route -> navigateTo(route) }
                )
            }
        }
    }
}
