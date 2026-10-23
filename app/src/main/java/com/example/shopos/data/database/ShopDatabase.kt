package com.example.shopos.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.shopos.data.dao.ShopDao
import com.example.shopos.data.model.*

@Database(
    entities = [
        BusinessProfile::class,
        Product::class,
        StockMovement::class,
        Sale::class,
        SaleItem::class,
        Customer::class,
        Supplier::class,
        Expense::class,
        CashMovement::class
    ],
    version = 2,
    exportSchema = false
)
abstract class ShopDatabase : RoomDatabase() {
    abstract fun shopDao(): ShopDao

    companion object {
        @Volatile
        private var INSTANCE: ShopDatabase? = null

        fun getDatabase(context: Context): ShopDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShopDatabase::class.java,
                    "shopos_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
