package com.example.shopos.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = ShopPrimaryLavender,
    onPrimary = ShopSurfaceWhite,
    primaryContainer = ShopPrimaryContainer,
    onPrimaryContainer = ShopOnPrimaryContainer,
    secondary = ShopPrimaryDark,
    onSecondary = ShopSurfaceWhite,
    background = ShopPearlBackground,
    onBackground = ShopTextDark,
    surface = ShopSurfaceWhite,
    onSurface = ShopTextDark,
    surfaceVariant = ShopPillBackground,
    onSurfaceVariant = ShopTextMedium,
    outline = ShopCardBorder,
    outlineVariant = ShopDivider,
    error = ShopBlushCoral,
    onError = ShopSurfaceWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = ShopPrimaryLavender,
    onPrimary = ShopSurfaceWhite,
    primaryContainer = ShopOnPrimaryContainer,
    onPrimaryContainer = ShopPrimaryContainer,
    secondary = ShopPrimaryLavender,
    onSecondary = ShopSurfaceWhite,
    background = Color(0xFF0F0E17),
    onBackground = Color(0xFFE8E7ED),
    surface = Color(0xFF1A1926),
    onSurface = Color(0xFFE8E7ED),
    surfaceVariant = Color(0xFF262538),
    onSurfaceVariant = Color(0xFFA5A4BA),
    outline = Color(0xFF333148),
    outlineVariant = Color(0xFF262538),
    error = ShopBlushCoral,
    onError = ShopSurfaceWhite
)

@Composable
fun ShopOSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
