package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.shopos.data.model.Product
import com.example.shopos.data.repository.ShopRepository
import com.example.shopos.util.Formatters
import org.junit.Assert.assertEquals
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
}
