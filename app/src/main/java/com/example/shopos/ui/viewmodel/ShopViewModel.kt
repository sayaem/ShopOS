package com.example.shopos.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopos.data.database.ShopDatabase
import com.example.shopos.data.model.*
import com.example.shopos.data.repository.ShopRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ShopDatabase.getDatabase(application)
    val repository = ShopRepository(db.shopDao())

    // --- Business Profile & App Settings ---
    val businessProfile = repository.businessProfile.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    // --- Products & Search/Filter ---
    val products = repository.allProducts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val lowStockProducts = repository.lowStockProducts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _productSearchQuery = MutableStateFlow("")
    val productSearchQuery: StateFlow<String> = _productSearchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    fun setProductSearchQuery(q: String) { _productSearchQuery.value = q }
    fun setSelectedCategory(cat: String) { _selectedCategory.value = cat }

    // --- POS Cart State ---
    private val _cartItems = MutableStateFlow<List<ShopRepository.CartItem>>(emptyList())
    val cartItems: StateFlow<List<ShopRepository.CartItem>> = _cartItems.asStateFlow()

    private val _cartDiscount = MutableStateFlow(0.0)
    val cartDiscount: StateFlow<Double> = _cartDiscount.asStateFlow()

    private val _cartPaymentMethod = MutableStateFlow("Cash")
    val cartPaymentMethod: StateFlow<String> = _cartPaymentMethod.asStateFlow()

    private val _cartCustomer = MutableStateFlow<Customer?>(null)
    val cartCustomer: StateFlow<Customer?> = _cartCustomer.asStateFlow()

    private val _lastCompletedSaleId = MutableStateFlow<Long?>(null)
    val lastCompletedSaleId: StateFlow<Long?> = _lastCompletedSaleId.asStateFlow()

    fun addToCart(product: Product) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val item = current[index]
            current[index] = item.copy(quantity = item.quantity + 1.0)
        } else {
            current.add(ShopRepository.CartItem(product = product, quantity = 1.0))
        }
        _cartItems.value = current
    }

    fun updateCartQuantity(productId: Long, delta: Double) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val newQty = current[index].quantity + delta
            if (newQty <= 0) {
                current.removeAt(index)
            } else {
                current[index] = current[index].copy(quantity = newQty)
            }
            _cartItems.value = current
        }
    }

    fun removeFromCart(productId: Long) {
        _cartItems.value = _cartItems.value.filterNot { it.product.id == productId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _cartDiscount.value = 0.0
        _cartCustomer.value = null
    }

    fun setCartPaymentMethod(method: String) {
        _cartPaymentMethod.value = method
    }

    fun setCartCustomer(customer: Customer?) {
        _cartCustomer.value = customer
    }

    fun completeSale(onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val items = _cartItems.value
            if (items.isEmpty()) return@launch
            val subtotal = items.sumOf { it.lineTotal }
            val discount = _cartDiscount.value
            val total = (subtotal - discount).coerceAtLeast(0.0)
            val customer = _cartCustomer.value

            val saleId = repository.completeSale(
                items = items,
                discount = discount,
                paidAmount = total, // full payment default in POS, can be partial
                paymentMethod = _cartPaymentMethod.value,
                customerId = customer?.id,
                customerName = customer?.name ?: "Walk-in Customer"
            )
            _lastCompletedSaleId.value = saleId
            clearCart()
            onSuccess(saleId)
        }
    }

    // --- Customers & Suppliers ---
    val customers = repository.allCustomers.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val suppliers = repository.allSuppliers.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val expenses = repository.allExpenses.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val sales = repository.allSales.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val cashMovements = repository.allCashMovements.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // --- Actions ---
    fun saveProduct(
        name: String,
        category: String,
        unit: String,
        purchaseCost: Double,
        sellingPrice: Double,
        stock: Double,
        lowStock: Double,
        barcode: String,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            repository.addProduct(
                Product(
                    name = name,
                    category = category,
                    unit = unit,
                    purchaseCost = purchaseCost,
                    sellingPrice = sellingPrice,
                    currentStock = stock,
                    lowStockThreshold = lowStock,
                    barcode = barcode
                )
            )
            onDone()
        }
    }

    fun adjustStock(productId: Long, delta: Double, reason: String, note: String) {
        viewModelScope.launch {
            repository.adjustStock(productId, delta, reason, note)
        }
    }

    fun addCustomer(name: String, phone: String, address: String, initialDue: Double) {
        viewModelScope.launch {
            repository.addCustomer(Customer(name = name, phone = phone, address = address, totalDue = initialDue))
        }
    }

    fun collectDue(customerId: Long, amount: Double, method: String, note: String) {
        viewModelScope.launch {
            repository.collectDue(customerId, amount, method, note)
        }
    }

    fun addSupplier(name: String, phone: String, contactPerson: String, initialPayable: Double) {
        viewModelScope.launch {
            repository.addSupplier(Supplier(name = name, phone = phone, contactPerson = contactPerson, totalPayable = initialPayable))
        }
    }

    fun paySupplier(supplierId: Long, amount: Double, method: String, note: String) {
        viewModelScope.launch {
            repository.paySupplier(supplierId, amount, method, note)
        }
    }

    fun addExpense(title: String, category: String, amount: Double, method: String) {
        viewModelScope.launch {
            repository.addExpense(Expense(title = title, category = category, amount = amount, paymentMethod = method))
        }
    }

    fun updateProfile(name: String, owner: String, phone: String, address: String, currency: String, lang: String) {
        viewModelScope.launch {
            val current = businessProfile.value ?: BusinessProfile()
            repository.saveBusinessProfile(
                current.copy(
                    shopName = name,
                    ownerName = owner,
                    phone = phone,
                    address = address,
                    currencySymbol = currency,
                    language = lang
                )
            )
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            val current = businessProfile.value ?: BusinessProfile()
            repository.saveBusinessProfile(current.copy(language = lang))
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetToFresh()
            repository.seedInitialDemoContent()
        }
    }

    init {
        viewModelScope.launch {
            repository.seedDemoDataIfEmpty()
        }
    }
}
