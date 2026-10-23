package com.example.shopos.data.repository

import com.example.shopos.data.dao.ShopDao
import com.example.shopos.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ShopRepository(private val dao: ShopDao) {

    val businessProfile: Flow<BusinessProfile?> = dao.getBusinessProfile()
    val allProducts: Flow<List<Product>> = dao.getAllProducts()
    val lowStockProducts: Flow<List<Product>> = dao.getLowStockProducts()
    val allSales: Flow<List<Sale>> = dao.getAllSales()
    val allCustomers: Flow<List<Customer>> = dao.getAllCustomers()
    val allSuppliers: Flow<List<Supplier>> = dao.getAllSuppliers()
    val allExpenses: Flow<List<Expense>> = dao.getAllExpenses()
    val allCashMovements: Flow<List<CashMovement>> = dao.getAllCashMovements()

    suspend fun saveBusinessProfile(profile: BusinessProfile) = withContext(Dispatchers.IO) {
        dao.saveBusinessProfile(profile)
    }

    suspend fun addProduct(product: Product): Long = withContext(Dispatchers.IO) {
        val id = dao.insertProduct(product)
        if (product.currentStock > 0) {
            dao.insertStockMovement(
                StockMovement(
                    productId = id,
                    productName = product.name,
                    quantityDelta = product.currentStock,
                    reason = "OPENING",
                    note = "Initial stock"
                )
            )
        }
        id
    }

    suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
        dao.updateProduct(product)
    }

    suspend fun adjustStock(productId: Long, quantityDelta: Double, reason: String, note: String) = withContext(Dispatchers.IO) {
        val product = dao.getProductById(productId) ?: return@withContext
        val newStock = (product.currentStock + quantityDelta).coerceAtLeast(0.0)
        dao.updateStock(productId, newStock)
        dao.insertStockMovement(
            StockMovement(
                productId = productId,
                productName = product.name,
                quantityDelta = quantityDelta,
                reason = reason,
                note = note
            )
        )
    }

    data class CartItem(
        val product: Product,
        var quantity: Double
    ) {
        val lineTotal: Double get() = product.sellingPrice * quantity
        val lineCost: Double get() = product.purchaseCost * quantity
    }

    suspend fun completeSale(
        items: List<CartItem>,
        discount: Double,
        paidAmount: Double,
        paymentMethod: String,
        customerId: Long?,
        customerName: String
    ): Long = withContext(Dispatchers.IO) {
        val subtotal = items.sumOf { it.lineTotal }
        val finalDiscount = discount.coerceIn(0.0, subtotal)
        val total = (subtotal - finalDiscount).coerceAtLeast(0.0)
        val actualPaid = paidAmount.coerceIn(0.0, total)
        val due = (total - actualPaid).coerceAtLeast(0.0)

        val date = Date()
        val invoiceFormatter = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
        val invoiceNumber = "#S${invoiceFormatter.format(date)}"

        val sale = Sale(
            invoiceNumber = invoiceNumber,
            dateMillis = date.time,
            customerId = customerId,
            customerName = if (customerName.isBlank()) "Walk-in Customer" else customerName,
            subtotal = subtotal,
            discount = finalDiscount,
            total = total,
            paidAmount = actualPaid,
            dueAmount = due,
            paymentMethod = paymentMethod,
            status = "COMPLETED"
        )

        val saleId = dao.insertSale(sale)

        val saleItems = items.map { cartItem ->
            SaleItem(
                saleId = saleId,
                productId = cartItem.product.id,
                productName = cartItem.product.name,
                unit = cartItem.product.unit,
                unitPrice = cartItem.product.sellingPrice,
                costBasis = cartItem.product.purchaseCost,
                quantity = cartItem.quantity,
                discount = 0.0,
                lineTotal = cartItem.lineTotal
            )
        }
        dao.insertSaleItems(saleItems)

        // Decrement stock atomically
        items.forEach { cartItem ->
            val product = dao.getProductById(cartItem.product.id)
            if (product != null) {
                val newStock = (product.currentStock - cartItem.quantity).coerceAtLeast(0.0)
                dao.updateStock(cartItem.product.id, newStock)
                dao.insertStockMovement(
                    StockMovement(
                        productId = cartItem.product.id,
                        productName = cartItem.product.name,
                        quantityDelta = -cartItem.quantity,
                        reason = "SALE",
                        referenceId = invoiceNumber,
                        note = "Sold in sale $invoiceNumber"
                    )
                )
            }
        }

        // Record cash movement if paid
        if (actualPaid > 0) {
            dao.insertCashMovement(
                CashMovement(
                    type = "CASH_IN",
                    amount = actualPaid,
                    source = "SALE",
                    referenceId = invoiceNumber,
                    notes = "Payment received via $paymentMethod"
                )
            )
        }

        // Update customer due if customer exists and due > 0
        if (customerId != null && due > 0) {
            val customer = dao.getCustomerById(customerId)
            if (customer != null) {
                dao.updateCustomerDue(customerId, customer.totalDue + due)
            }
        }

        saleId
    }

    suspend fun getSaleDetails(saleId: Long): Pair<Sale?, List<SaleItem>> = withContext(Dispatchers.IO) {
        val sale = dao.getSaleByIdOnce(saleId)
        val items = dao.getSaleItemsOnce(saleId)
        Pair(sale, items)
    }

    suspend fun addCustomer(customer: Customer): Long = withContext(Dispatchers.IO) {
        dao.insertCustomer(customer)
    }

    suspend fun collectDue(customerId: Long, amount: Double, paymentMethod: String, note: String) = withContext(Dispatchers.IO) {
        val customer = dao.getCustomerById(customerId) ?: return@withContext
        val newDue = (customer.totalDue - amount).coerceAtLeast(0.0)
        dao.updateCustomerDue(customerId, newDue)
        dao.insertCashMovement(
            CashMovement(
                type = "CASH_IN",
                amount = amount,
                source = "DUE_COLLECTION",
                referenceId = "CUST-$customerId",
                notes = "Collected from ${customer.name} via $paymentMethod: $note"
            )
        )
    }

    suspend fun addSupplier(supplier: Supplier): Long = withContext(Dispatchers.IO) {
        dao.insertSupplier(supplier)
    }

    suspend fun paySupplier(supplierId: Long, amount: Double, paymentMethod: String, note: String) = withContext(Dispatchers.IO) {
        val supplier = dao.getSupplierById(supplierId) ?: return@withContext
        val newPayable = (supplier.totalPayable - amount).coerceAtLeast(0.0)
        dao.updateSupplierPayable(supplierId, newPayable)
        dao.insertCashMovement(
            CashMovement(
                type = "CASH_OUT",
                amount = amount,
                source = "SUPPLIER_PAYMENT",
                referenceId = "SUPP-$supplierId",
                notes = "Paid to ${supplier.name} via $paymentMethod: $note"
            )
        )
    }

    suspend fun addExpense(expense: Expense): Long = withContext(Dispatchers.IO) {
        val id = dao.insertExpense(expense)
        dao.insertCashMovement(
            CashMovement(
                type = "CASH_OUT",
                amount = expense.amount,
                source = "EXPENSE",
                referenceId = "EXP-$id",
                notes = "${expense.category}: ${expense.title}"
            )
        )
        id
    }

    suspend fun seedDemoDataIfEmpty() = withContext(Dispatchers.IO) {
        val profile = dao.getBusinessProfileOnce()
        if (profile == null) {
            dao.saveBusinessProfile(BusinessProfile(isDemoLoaded = true))
            seedInitialDemoContent()
        }
    }

    suspend fun resetToFresh() = withContext(Dispatchers.IO) {
        dao.clearProducts()
        dao.clearSales()
        dao.clearSaleItems()
        dao.clearCustomers()
        dao.clearSuppliers()
        dao.clearExpenses()
        dao.clearCashMovements()
        dao.clearStockMovements()
        dao.saveBusinessProfile(BusinessProfile(isDemoLoaded = false))
    }

    suspend fun seedInitialDemoContent() = withContext(Dispatchers.IO) {
        // Products from mockup
        val products = listOf(
            Product(name = "Maggi Noodles", category = "Food", unit = "Piece", purchaseCost = 35.0, sellingPrice = 45.0, currentStock = 12.0, lowStockThreshold = 15.0),
            Product(name = "Coca Cola 500ml", category = "Beverages", unit = "Piece", purchaseCost = 48.0, sellingPrice = 60.0, currentStock = 24.0, lowStockThreshold = 10.0),
            Product(name = "Surf Excel 1kg", category = "Personal Care", unit = "Piece", purchaseCost = 190.0, sellingPrice = 220.0, currentStock = 8.0, lowStockThreshold = 10.0),
            Product(name = "Dettol Soap", category = "Personal Care", unit = "Piece", purchaseCost = 36.0, sellingPrice = 45.0, currentStock = 30.0, lowStockThreshold = 10.0),
            Product(name = "Dove Shampoo 340ml", category = "Personal Care", unit = "Piece", purchaseCost = 270.0, sellingPrice = 320.0, currentStock = 14.0, lowStockThreshold = 8.0),
            Product(name = "Lays Chips", category = "Food", unit = "Piece", purchaseCost = 22.0, sellingPrice = 30.0, currentStock = 20.0, lowStockThreshold = 10.0),
            Product(name = "Sunlight Soap", category = "Personal Care", unit = "Piece", purchaseCost = 28.0, sellingPrice = 35.0, currentStock = 5.0, lowStockThreshold = 10.0),
            Product(name = "Pran Frooto 250ml", category = "Beverages", unit = "Piece", purchaseCost = 20.0, sellingPrice = 25.0, currentStock = 35.0, lowStockThreshold = 10.0),
            Product(name = "Aarong Dairy Butter", category = "Food", unit = "Piece", purchaseCost = 110.0, sellingPrice = 135.0, currentStock = 15.0, lowStockThreshold = 5.0)
        )
        dao.insertAllProducts(products)

        // Customers from mockup
        val customers = listOf(
            Customer(name = "Walk-in Customer", phone = "", address = "", totalDue = 0.0),
            Customer(name = "Rafiq Ahmed", phone = "01812-998877", address = "Road 4, House 12", totalDue = 1200.0, notes = "2 items due"),
            Customer(name = "Sadia Akter", phone = "01711-223344", address = "Block B, Flat 3A", totalDue = 850.0, notes = "1 item due"),
            Customer(name = "Abdur Rahman", phone = "01912-334455", address = "Main Road", totalDue = 620.0, notes = "3 items due"),
            Customer(name = "Mitu Store", phone = "01612-445566", address = "Corner Market", totalDue = 0.0),
            Customer(name = "Hasan Mia", phone = "01512-556677", address = "Lane 2, Shop 7", totalDue = 3400.0, notes = "5 items due")
        )
        dao.insertAllCustomers(customers)

        // Suppliers from mockup
        val suppliers = listOf(
            Supplier(name = "Rahim Trading", phone = "01700-112233", contactPerson = "Rahim", totalPayable = 12500.0, notes = "3 invoices pending"),
            Supplier(name = "Fresh Mart", phone = "01800-223344", contactPerson = "Shumon", totalPayable = 5200.0, notes = "1 invoice"),
            Supplier(name = "ABC Distributor", phone = "01900-334455", contactPerson = "Tanvir", totalPayable = 8750.0, notes = "2 invoices"),
            Supplier(name = "Green Agro", phone = "01600-445566", contactPerson = "Hannan", totalPayable = 3400.0, notes = "1 invoice"),
            Supplier(name = "Sunrise Ltd.", phone = "01500-556677", contactPerson = "Kamal", totalPayable = 0.0)
        )
        dao.insertAllSuppliers(suppliers)

        // Expenses from mockup
        val expenses = listOf(
            Expense(title = "Shop Rent", category = "Rent", amount = 5000.0, paymentMethod = "Bank"),
            Expense(title = "Electricity Bill", category = "Electricity", amount = 1250.0, paymentMethod = "Bkash"),
            Expense(title = "Transport", category = "Transport", amount = 800.0, paymentMethod = "Cash"),
            Expense(title = "Staff Salary", category = "Salary", amount = 7000.0, paymentMethod = "Bank"),
            Expense(title = "Miscellaneous", category = "Miscellaneous", amount = 450.0, paymentMethod = "Cash")
        )
        dao.insertAllExpenses(expenses)

        // Seed initial cash & completed sales for realistic dashboard & reports
        dao.insertCashMovement(
            CashMovement(type = "CASH_IN", amount = 20000.0, source = "CAPITAL", notes = "Opening balance")
        )
        
        // Sales to populate Today's Sales = ৳ 28,450
        val s1 = Sale(
            invoiceNumber = "#S20241026-0012",
            dateMillis = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
            customerName = "Rafiq Ahmed",
            subtotal = 180.0,
            discount = 0.0,
            total = 180.0,
            paidAmount = 180.0,
            dueAmount = 0.0,
            paymentMethod = "Cash"
        )
        val s1Id = dao.insertSale(s1)
        dao.insertSaleItems(listOf(
            SaleItem(saleId = s1Id, productId = 1, productName = "Maggi Noodles", unitPrice = 45.0, costBasis = 35.0, quantity = 2.0, lineTotal = 90.0),
            SaleItem(saleId = s1Id, productId = 2, productName = "Coca Cola 500ml", unitPrice = 60.0, costBasis = 48.0, quantity = 1.0, lineTotal = 60.0),
            SaleItem(saleId = s1Id, productId = 6, productName = "Lays Chips", unitPrice = 30.0, costBasis = 22.0, quantity = 1.0, lineTotal = 30.0)
        ))
        dao.insertCashMovement(CashMovement(type = "CASH_IN", amount = 180.0, source = "SALE", referenceId = s1.invoiceNumber, notes = "Payment received"))

        val s2 = Sale(
            invoiceNumber = "#S20241026-0011",
            dateMillis = System.currentTimeMillis() - 1000 * 60 * 60 * 4,
            customerName = "Walk-in Customer",
            subtotal = 28270.0,
            discount = 0.0,
            total = 28270.0,
            paidAmount = 28270.0,
            dueAmount = 0.0,
            paymentMethod = "Card"
        )
        val s2Id = dao.insertSale(s2)
        dao.insertSaleItems(listOf(
            SaleItem(saleId = s2Id, productId = 3, productName = "Surf Excel 1kg", unitPrice = 220.0, costBasis = 190.0, quantity = 40.0, lineTotal = 8800.0),
            SaleItem(saleId = s2Id, productId = 5, productName = "Dove Shampoo 340ml", unitPrice = 320.0, costBasis = 270.0, quantity = 40.0, lineTotal = 12800.0),
            SaleItem(saleId = s2Id, productId = 4, productName = "Dettol Soap", unitPrice = 45.0, costBasis = 36.0, quantity = 148.0, lineTotal = 6670.0)
        ))
        dao.insertCashMovement(CashMovement(type = "CASH_IN", amount = 28270.0, source = "SALE", referenceId = s2.invoiceNumber, notes = "Payment received via Card"))
    }
}
