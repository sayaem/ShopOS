package com.example.shopos.data.dao

import androidx.room.*
import com.example.shopos.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {

    // --- Business Profile ---
    @Query("SELECT * FROM business_profile WHERE id = 1 LIMIT 1")
    fun getBusinessProfile(): Flow<BusinessProfile?>

    @Query("SELECT * FROM business_profile WHERE id = 1 LIMIT 1")
    suspend fun getBusinessProfileOnce(): BusinessProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBusinessProfile(profile: BusinessProfile)

    // --- Products ---
    @Query("SELECT * FROM products WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE isArchived = 0 AND currentStock <= lowStockThreshold ORDER BY currentStock ASC")
    fun getLowStockProducts(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Query("UPDATE products SET currentStock = :newStock WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Double)

    @Delete
    suspend fun deleteProduct(product: Product)

    // --- Stock Movements ---
    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC")
    fun getAllStockMovements(): Flow<List<StockMovement>>

    @Insert
    suspend fun insertStockMovement(movement: StockMovement): Long

    // --- Sales ---
    @Query("SELECT * FROM sales ORDER BY dateMillis DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    fun getSaleById(id: Long): Flow<Sale?>

    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    suspend fun getSaleByIdOnce(id: Long): Sale?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Update
    suspend fun updateSale(sale: Sale)

    // --- Sale Items ---
    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    fun getSaleItems(saleId: Long): Flow<List<SaleItem>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItemsOnce(saleId: Long): List<SaleItem>

    @Query("SELECT * FROM sale_items")
    fun getAllSaleItems(): Flow<List<SaleItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItem>)

    // --- Customers ---
    @Query("SELECT * FROM customers WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Long): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCustomers(customers: List<Customer>)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("UPDATE customers SET totalDue = :newDue WHERE id = :id")
    suspend fun updateCustomerDue(id: Long, newDue: Double)

    // --- Suppliers ---
    @Query("SELECT * FROM suppliers WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers WHERE id = :id LIMIT 1")
    suspend fun getSupplierById(id: Long): Supplier?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSuppliers(suppliers: List<Supplier>)

    @Update
    suspend fun updateSupplier(supplier: Supplier)

    @Query("UPDATE suppliers SET totalPayable = :newPayable WHERE id = :id")
    suspend fun updateSupplierPayable(id: Long, newPayable: Double)

    // --- Expenses ---
    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllExpenses(expenses: List<Expense>)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    // --- Cash Movements ---
    @Query("SELECT * FROM cash_movements ORDER BY dateMillis DESC")
    fun getAllCashMovements(): Flow<List<CashMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashMovement(movement: CashMovement): Long

    // --- Maintenance / Reset ---
    @Query("DELETE FROM products")
    suspend fun clearProducts()

    @Query("DELETE FROM sales")
    suspend fun clearSales()

    @Query("DELETE FROM sale_items")
    suspend fun clearSaleItems()

    @Query("DELETE FROM customers")
    suspend fun clearCustomers()

    @Query("DELETE FROM suppliers")
    suspend fun clearSuppliers()

    @Query("DELETE FROM expenses")
    suspend fun clearExpenses()

    @Query("DELETE FROM cash_movements")
    suspend fun clearCashMovements()

    @Query("DELETE FROM stock_movements")
    suspend fun clearStockMovements()
}
