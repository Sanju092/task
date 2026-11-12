package com.yearplanner.ui.theme

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.yearplanner.R

// ============================================================================
// Color Schemes
// ============================================================================

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),
    primaryContainer = Color(0xFF1A2B44),
    onPrimary = Color(0xFF000000),
    onPrimaryContainer = Color(0xFFE8F0FE),
    
    secondary = Color(0xFF4DB6AC),
    secondaryContainer = Color(0xFF003D3A),
    onSecondary = Color(0xFF000000),
    onSecondaryContainer = Color(0xFFA7F3E0),
    
    tertiary = Color(0xFFFFB74D),
    tertiaryContainer = Color(0xFF3E2A00),
    onTertiary = Color(0xFF000000),
    onTertiaryContainer = Color(0xFFFFE5B4),
    
    error = Color(0xFFF2B8B5),
    errorContainer = Color(0xFF8C1D18),
    onError = Color(0xFF601410),
    onErrorContainer = Color(0xFFFCEDED),
    
    surface = Color(0xFF121212),
    surfaceContainer = Color(0xFF1E1E1E),
    surfaceContainerHigh = Color(0xFF2C2C2C),
    surfaceContainerHighest = Color(0xFF363636),
    onSurface = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFFB0B0B0),
    
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFF4A4A4A),
    
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    
    inverseSurface = Color(0xFFE0E0E0),
    inverseOnSurface = Color(0xFF121212),
    
    scrim = Color(0xFF000000)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    primaryContainer = Color(0xFFBBDEFB),
    onPrimary = Color(0xFFFFFFFF),
    onPrimaryContainer = Color(0xFF001D35),
    
    secondary = Color(0xFF00897B),
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondary = Color(0xFFFFFFFF),
    onSecondaryContainer = Color(0xFF00201E),
    
    tertiary = Color(0xFFF57C00),
    tertiaryContainer = Color(0xFFFFF3E0),
    onTertiary = Color(0xFFFFFFFF),
    onTertiaryContainer = Color(0xFF3E2A00),
    
    error = Color(0xFFD32F2F),
    errorContainer = Color(0xFFFFEBEE),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF410002),
    
    surface = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFF5F5F5),
    surfaceContainerHigh = Color(0xFFEEEEEE),
    surfaceContainerHighest = Color(0xFFE8E8E8),
    onSurface = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFF444444),
    
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFFC4C4C4),
    
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A1A1A),
    
    inverseSurface = Color(0xFF1A1A1A),
    inverseOnSurface = Color(0xFFFFFFFF),
    
    scrim = Color(0xFF000000)
)

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

// ============================================================================
// Custom Typography
// ============================================================================

val AppTypography = Typography(
    displayLarge = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = -0.25.sp
    ),
    displayMedium = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = androidx.compose.material3.TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// Custom shapes
val AppShapes = androidx.compose.material3.Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
)

// Custom shadows
val AppShadows = listOf(
    Shadow(
        offset = androidx.compose.ui.geometry.Offset(0f, 1f),
        blurRadius = 3f,
        color = Color.Black.copy(alpha = 0.12f)
    ),
    Shadow(
        offset = androidx.compose.ui.geometry.Offset(0f, 4f),
        blurRadius = 8f,
        color = Color.Black.copy(alpha = 0.08f)
    ),
    Shadow(
        offset = androidx.compose.ui.geometry.Offset(0f, 8f),
        blurRadius = 16f,
        color = Color.Black.copy(alpha = 0.06f)
    )
)

// ============================================================================
// Main Theme Composable
// ============================================================================

@Composable
fun Theme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    useDynamicColors: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    
    val isSystemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    
    // Use dynamic colors on Android 12+ if enabled
    val colorScheme = if (useDynamicColors && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (darkTheme) DarkColorScheme else LightColorScheme
    }
    
    // Window insets and status bar
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                window.statusBarColor = colorScheme.surfaceContainer.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                
                // Set system bar icons theme
                val isDark = darkTheme
                window.decorView.systemUiVisibility = if (isDark) 0 else android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}

// ============================================================================
// Card Elevation Extension
// ============================================================================

@Composable
fun ElevatedCard(
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    colors: androidx.compose.material3.CardColors = CardDefaults.elevatedCardColors(),
    elevation: androidx.compose.material3.CardElevation = CardDefaults.elevatedCardElevation(),
    shape: androidx.compose.ui.graphics.Shape = CardDefaults.elevatedShape,
    border: androidx.compose.foundation.BorderStroke? = null,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.ElevatedCard(
        modifier = modifier,
        colors = colors,
        elevation = elevation,
        shape = shape,
        border = border,
        content = { content() }
    )
}