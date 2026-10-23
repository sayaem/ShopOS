package com.example.shopos.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object Formatters {
    fun formatCurrency(amount: Double, symbol: String = "৳"): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
            minimumFractionDigits = 0
        }
        return "$symbol ${formatter.format(amount)}"
    }

    fun formatDate(timeMillis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
        return sdf.format(Date(timeMillis))
    }

    fun formatDateShort(timeMillis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
        return sdf.format(Date(timeMillis))
    }
}

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    BANGLA("bn", "বাংলা")
}

object ShopOSStrings {
    private val en = mapOf(
        "app_name" to "ShopOS",
        "tagline" to "Your Shop. Smarter.",
        "bangla_tagline" to "আপনার দোকান, এখন আরও সহজভাবে",
        "complete_shop_mgmt" to "Complete Shop Management",
        "good_morning" to "Good Morning,",
        "make_today_count" to "Let's make today count!",
        "todays_sales" to "Today's Sales",
        "vs_yesterday" to "vs yesterday",
        "total_profit" to "Total Profit",
        "cash_in_hand" to "Cash in Hand",
        "due_collect" to "Due (To Collect)",
        "due_pay" to "Due (To Pay)",
        "low_stock_alert" to "Low Stock Alert",
        "products_running_low" to "products are running low",
        "nav_home" to "Home",
        "nav_products" to "Products",
        "nav_sales" to "Sales",
        "nav_reports" to "Reports",
        "nav_more" to "More",
        "search_products" to "Search products...",
        "search_barcode" to "Search product / barcode...",
        "search_customer" to "Search customer...",
        "search_supplier" to "Search supplier...",
        "category_all" to "All",
        "category_food" to "Food",
        "category_beverages" to "Beverages",
        "category_personal_care" to "Personal Care",
        "stock" to "Stock",
        "low" to "Low",
        "new_sale" to "New Sale",
        "subtotal" to "Subtotal",
        "discount" to "Discount",
        "total" to "Total",
        "cash" to "Cash",
        "card" to "Card",
        "bkash" to "Bkash",
        "nagad" to "Nagad",
        "complete_sale" to "Complete Sale",
        "sale_receipt" to "Sale Receipt",
        "payment_successful" to "Payment Successful",
        "invoice_no" to "Invoice No",
        "print_receipt" to "Print Receipt",
        "share" to "Share",
        "add_product" to "Add Product",
        "product_name" to "Product Name",
        "category" to "Category",
        "unit" to "Unit",
        "purchase_price" to "Purchase Price",
        "selling_price" to "Selling Price",
        "barcode_optional" to "Barcode (optional)",
        "save_product" to "Save Product",
        "inventory" to "Inventory",
        "in_stock" to "In Stock",
        "out_of_stock" to "Out of Stock",
        "customers" to "Customers",
        "suppliers" to "Suppliers",
        "expenses" to "Expenses",
        "daily" to "Daily",
        "monthly" to "Monthly",
        "weekly" to "Weekly",
        "days_7" to "7 Days",
        "days_30" to "30 Days",
        "months_3" to "3 Months",
        "custom" to "Custom",
        "total_purchase" to "Total Purchase",
        "sales_report" to "Sales Report",
        "top_selling_products" to "Top Selling Products",
        "profit_and_loss" to "Profit & Loss",
        "net_profit" to "Net Profit",
        "cost_of_goods" to "Cost of Goods",
        "settings" to "Settings",
        "business_profile" to "Business Profile",
        "language" to "Language",
        "backup_restore" to "Backup & Restore",
        "help_support" to "Help & Support",
        "rate_us" to "Rate Us",
        "privacy_policy" to "Privacy Policy",
        "terms_conditions" to "Terms & Conditions",
        "appearance" to "Appearance",
        "notifications" to "Notifications",
        "collect_due" to "Collect Due",
        "pay_supplier" to "Pay Supplier",
        "adjust_stock" to "Adjust Stock",
        "add_expense" to "Add Expense",
        "add_customer" to "Add Customer",
        "add_supplier" to "Add Supplier"
    )

    private val bn = mapOf(
        "app_name" to "শপ ওএস",
        "tagline" to "আপনার দোকান, এখন আরও সহজভাবে",
        "bangla_tagline" to "আপনার দোকান, এখন আরও সহজভাবে",
        "complete_shop_mgmt" to "সম্পূর্ণ দোকান ব্যবস্থাপনা",
        "good_morning" to "শুভ সকাল,",
        "make_today_count" to "আজকের দিনটি সফল হোক!",
        "todays_sales" to "আজকের বিক্রি",
        "vs_yesterday" to "গতকালের চেয়ে",
        "total_profit" to "মোট লাভ",
        "cash_in_hand" to "নগদ টাকা",
        "due_collect" to "পাওনা বাকি (আদায়)",
        "due_pay" to "দেনা বাকি (পরিশোধ)",
        "low_stock_alert" to "স্টক সতর্কতা",
        "products_running_low" to "টি পণ্যের স্টক কমে এসেছে",
        "nav_home" to "হোম",
        "nav_products" to "পণ্য",
        "nav_sales" to "বিক্রি",
        "nav_reports" to "রিপোর্ট",
        "nav_more" to "অন্যান্য",
        "search_products" to "পণ্য খুঁজুন...",
        "search_barcode" to "পণ্য বা বারকোড খুঁজুন...",
        "search_customer" to "গ্রাহক খুঁজুন...",
        "search_supplier" to "সরবরাহকারী খুঁজুন...",
        "category_all" to "সব",
        "category_food" to "খাবার",
        "category_beverages" to "পানীয়",
        "category_personal_care" to "ব্যক্তিগত যত্ন",
        "stock" to "স্টক",
        "low" to "কম",
        "new_sale" to "নতুন বিক্রি",
        "subtotal" to "উপমোট",
        "discount" to "ছাড়",
        "total" to "সর্বমোট",
        "cash" to "নগদ",
        "card" to "কার্ড",
        "bkash" to "বিকাশ",
        "nagad" to "নগদ (ওয়ালেট)",
        "complete_sale" to "বিক্রি সম্পন্ন করুন",
        "sale_receipt" to "বিক্রির রশিদ",
        "payment_successful" to "পেমেন্ট সফল হয়েছে",
        "invoice_no" to "চালান নং",
        "print_receipt" to "রশিদ প্রিন্ট",
        "share" to "শেয়ার করুন",
        "add_product" to "নতুন পণ্য যোগ",
        "product_name" to "পণ্যের নাম",
        "category" to "ক্যাটাগরি",
        "unit" to "একক",
        "purchase_price" to "ক্রয় মূল্য",
        "selling_price" to "বিক্রয় মূল্য",
        "barcode_optional" to "বারকোড (ঐচ্ছিক)",
        "save_product" to "পণ্য সংরক্ষণ করুন",
        "inventory" to "ইনভেন্টরি",
        "in_stock" to "স্টকে আছে",
        "out_of_stock" to "স্টক শেষ",
        "customers" to "গ্রাহকবৃন্দ",
        "suppliers" to "সরবরাহকারী",
        "expenses" to "খরচসমূহ",
        "daily" to "দৈনিক",
        "monthly" to "মাসিক",
        "weekly" to "সাপ্তাহিক",
        "days_7" to "৭ দিন",
        "days_30" to "৩০ দিন",
        "months_3" to "৩ মাস",
        "custom" to "কাস্টম",
        "total_purchase" to "মোট ক্রয়",
        "sales_report" to "বিক্রি রিপোর্ট",
        "top_selling_products" to "সর্বাধিক বিক্রিত পণ্য",
        "profit_and_loss" to "লাভ ও ক্ষতি",
        "net_profit" to "নিট লাভ",
        "cost_of_goods" to "পণ্যের ক্রয়মূল্য",
        "settings" to "সেটিংস",
        "business_profile" to "দোকানের তথ্য",
        "language" to "ভাষা",
        "backup_restore" to "ব্যাকআপ ও রিস্টোর",
        "help_support" to "সাহায্য ও সাপোর্ট",
        "rate_us" to "রেটিং দিন",
        "privacy_policy" to "প্রাইভেসি পলিসি",
        "terms_conditions" to "শর্তাবলী",
        "appearance" to "থিম",
        "notifications" to "নোটিফিকেশন",
        "collect_due" to "বাকি আদায়",
        "pay_supplier" to "দেনা পরিশোধ",
        "adjust_stock" to "স্টক সমন্বয়",
        "add_expense" to "খরচ যুক্ত করুন",
        "add_customer" to "নতুন গ্রাহক",
        "add_supplier" to "নতুন সরবরাহকারী"
    )

    fun get(key: String, lang: String = "en"): String {
        return if (lang == "bn") {
            bn[key] ?: en[key] ?: key
        } else {
            en[key] ?: key
        }
    }
}
