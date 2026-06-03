package com.bizcopay.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BizcopayColorScheme = darkColorScheme(
    primary          = BizcoBlue,
    onPrimary        = BizcoTextPrimary,
    secondary        = BizcoOrange,
    onSecondary      = BizcoBackground,
    background       = BizcoBackground,
    onBackground     = BizcoTextPrimary,
    surface          = BizcoCard,
    onSurface        = BizcoTextPrimary,
    surfaceVariant   = BizcoSurface,
    onSurfaceVariant = BizcoTextSecondary,
    error            = BizcoError,
    onError          = BizcoTextPrimary,
)

@Composable
fun BizcopayTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BizcoBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = BizcopayColorScheme,
        typography  = Typography,
        content     = content
    )
}
