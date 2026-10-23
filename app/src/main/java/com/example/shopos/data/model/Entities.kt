package com.example.shopos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_profile")
data class BusinessProfile(
    @PrimaryKey val id: Int = 1,
    val shopName: String = "My Shop",
    val ownerName: String = "Rahim Uddin",
    val email: String = "owner@shopos.app",
    val phone: String = "",
    val address: String = "",
    val businessType: String = "General Retail",
    val currencySymbol: String = "৳",
    val language: String = "en", // "en" or "bn"
    val isDemoLoaded: Boolean = false,
    val isOnboardingCompleted: Boolean = false,
    val startingCash: Double = 0.0
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "General",
    val unit: String = "Piece",
    val purchaseCost: Double,
    val sellingPrice: Double,
    val currentStock: Double,
    val lowStockThreshold: Double = 10.0,
    val sku: String = "",
    val barcode: String = "",
    val notes: String = "",
    val imageResName: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stock_movements")
data class StockMovement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val productName: String,
    val quantityDelta: Double,
    val reason: String, // "SALE", "PURCHASE", "RESTOCK", "DAMAGE", "OPENING", "MANUAL"
    val referenceId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val customerId: Long? = null,
    val customerName: String = "Walk-in Customer",
    val subtotal: Double,
    val discount: Double = 0.0,
    val total: Double,
    val paidAmount: Double,
    val dueAmount: Double = 0.0,
    val paymentMethod: String = "Cash", // "Cash", "Card", "Bkash", "Nagad"
    val status: String = "COMPLETED", // "COMPLETED", "VOIDED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sale_items")
data class SaleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val unit: String = "Piece",
    val unitPrice: Double,
    val costBasis: Double,
    val quantity: Double,
    val discount: Double = 0.0,
    val lineTotal: Double
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val totalDue: Double = 0.0,
    val notes: String = "",
    val isArchived: Boolean = false
)

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val contactPerson: String = "",
    val totalPayable: Double = 0.0,
    val notes: String = "",
    val isArchived: Boolean = false
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // "Rent", "Electricity", "Transport", "Salary", "Maintenance", "Miscellaneous"
    val amount: Double,
    val dateMillis: Long = System.currentTimeMillis(),
    val paymentMethod: String = "Cash",
    val note: String = ""
)

@Entity(tableName = "cash_movements")
data class CashMovement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "CASH_IN", "CASH_OUT"
    val amount: Double,
    val source: String, // "SALE", "DUE_COLLECTION", "EXPENSE", "SUPPLIER_PAYMENT", "CAPITAL"
    val referenceId: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)
