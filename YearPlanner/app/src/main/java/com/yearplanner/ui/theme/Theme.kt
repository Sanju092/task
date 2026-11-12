package com.yearplanner.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.yearplanner.R

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),
    primaryContainer = Color(0xFF1A2B44),
    secondary = Color(0xFF4DB6AC),
    secondaryContainer = Color(0xFF003D3A),
    tertiary = Color(0xFFFFB74D),
    tertiaryContainer = Color(0xFF3E2A00),
    surface = Color(0xFF121212),
    surfaceContainer = Color(0xFF1E1E1E),
    surfaceContainerHigh = Color(0xFF2C2C2C),
    background = Color(0xFF121212),
    onPrimary = Color(0xFF000000),
    onSecondary = Color(0xFF000000),
    onTertiary = Color(0xFF000000),
    onSurface = Color(0xFFE0E0E0),
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFF4A4A4A),
    error = Color(0xFFE57373),
    errorContainer = Color(0xFF3E1A1A)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    primaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFF00897B),
    secondaryContainer = Color(0xFFB2DFDB),
    tertiary = Color(0xFFF57C00),
    tertiaryContainer = Color(0xFFFFF3E0),
    surface = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFF5F5F5),
    surfaceContainerHigh = Color(0xFFEEEEEE),
    background = Color(0xFFFFFFFF),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A1A),
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFFBDBDBD),
    error = Color(0xFFD32F2F),
    errorContainer = Color(0xFFFFEBEE)
)

@Composable
fun Theme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                window.statusBarColor = colorScheme.surfaceContainer.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
            }
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

val Typography = androidx.compose.material3.Typography()