package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.shopos.data.model.Customer
import com.example.shopos.data.model.Product
import com.example.shopos.data.model.Supplier
import com.example.shopos.data.repository.ShopRepository
import com.example.shopos.util.Formatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies ShopOS app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ShopOS", appName)
    }

    @Test
    fun `cart line item and total calculations are exact`() {
        val product = Product(
            id = 1,
            name = "Maggi Noodles",
            category = "Food",
            unit = "Piece",
            purchaseCost = 35.0,
            sellingPrice = 45.0,
            currentStock = 12.0
        )
        val cartItem = ShopRepository.CartItem(product = product, quantity = 2.0)
        assertEquals(90.0, cartItem.lineTotal, 0.001)
        assertEquals(70.0, cartItem.lineCost, 0.001)
    }

    @Test
    fun `currency formatter formats taka symbol accurately`() {
        val formatted = Formatters.formatCurrency(28450.0, "৳")
        assertEquals("৳ 28,450", formatted)
    }

    @Test
    fun `customer outstanding balance and reduction logic`() {
        val customer = Customer(
            id = 2,
            name = "Rahim Mia",
            phone = "+880 1712-345678",
            totalDue = 4850.0
        )
        val paymentAmount = 1500.0
        val remainingDue = (customer.totalDue - paymentAmount).coerceAtLeast(0.0)
        assertEquals(3350.0, remainingDue, 0.001)
    }

    @Test
    fun `supplier payable reduction logic`() {
        val supplier = Supplier(
            id = 1,
            name = "Square Consumer Products Ltd",
            phone = "+880 1711-223344",
            totalPayable = 6200.0
        )
        val paymentAmount = 2000.0
        val remainingPayable = (supplier.totalPayable - paymentAmount).coerceAtLeast(0.0)
        assertEquals(4200.0, remainingPayable, 0.001)
    }
}
