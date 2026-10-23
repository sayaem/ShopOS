# ShopOS Audit Report & Restoration Plan

## 1. Project Architecture & State
- **Framework:** Modern Android with Kotlin, Jetpack Compose, Material 3, ViewModel, Kotlin Coroutines & Flow.
- **Data Persistence:** Offline-first Room Database (`ShopDatabase`) with SQLite backend.
- **Entry Point:** `MainActivity.kt` managing navigation state with a back stack.
- **State Management:** MVVM pattern with `ShopViewModel` exposing `StateFlow` from `ShopRepository` and `ShopDao`.

---

## 2. Root Cause Analysis: More Tab Crash (RESOLVED)
- **Error:** When tapping the **More** tab, Compose crashes with `XmlPullParserException: <layer-list> is not supported` via `painterResource`.
- **Root Cause:** `MoreScreen.kt` line 125 called `painterResource(id = R.drawable.ic_launcher_foreground)`. Since `ic_launcher_foreground.xml` is an Android `<layer-list>` adaptive icon drawable, Compose's `painterResource` throws `IllegalArgumentException` / `XmlPullParserException` because Compose only supports `<vector>` or raster bitmaps.
- **Resolution:** Generated and integrated `ic_shopos_logo.png` bitmap drawable in `res/drawable/` and updated `MoreScreen` and `SplashScreen` to use it safely.

---

## 3. Audit of Mock Data & Hardcoded Business Figures
1. **Dashboard (`HomeScreen.kt`):**
   - Fallback hardcoded values (`28450.0`, `7320.0`, `12600.0`, `4850.0`, `6200.0`) when DB has no records.
   - Must show genuine 0 or designed empty state when database has no recorded transactions.
2. **Reports (`ReportsScreen.kt`, `SalesReportScreen.kt`, `ProfitLossScreen.kt`):**
   - Hardcoded totals (`125480.0`, `78320.0`, `12450.0`, `24560.0`) and static top-selling products list.
   - Must calculate dynamically from `sales`, `sale_items`, `expenses`, and `cash_movements`.
3. **Demo Mode Isolation:**
   - Database auto-seeded fake sales and expenses on startup.
   - Needs strict separation: fresh install starts with clean store (or user choice during onboarding). Demo data should only be loaded if explicitly requested via "Load Sample Data", with a clear banner and "Reset to Fresh" option.

---

## 4. Interaction Inventory & Functional Gaps
- **Splash & First-Launch Onboarding:** Missing onboarding flow. App currently goes straight to Dashboard. Needs first-launch setup (Shop Name, Currency, Language) and persistent completion flag.
- **New Sale & Checkout:** Receipt sharing & printing need real Android Intent and PrintManager implementations. Duplicate submission prevention needed.
- **Expenses Screen:** Filter chips ("Daily", "Monthly", "All") need real timestamp filtering.
- **Reports Screen:** Time filters ("7 Days", "30 Days", "3 Months") need dynamic date boundary queries.
- **Backup & Restore:** Implement actual JSON export of store database to internal/shared storage and real restore validation.

---

## 5. Execution Plan
1. **Stage A:** Fix More tab crash and splash logo references.
2. **Stage B:** First-launch Onboarding & Business Setup flow with persistence.
3. **Stage C:** Real financial calculations in Repository & ViewModel (no hardcoded fallbacks).
4. **Stage D:** Make all buttons functional (Print, Share, Export/Import Backup, Expense filters, Reports time filters, Product edits).
5. **Stage E:** Verification, compilation, and unit tests.
