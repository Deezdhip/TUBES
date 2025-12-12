package com.example.tubes.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Slate Blue Dark Theme
 */
private val AppColorScheme = darkColorScheme(
    // Primary colors
    primary = PrimaryBlue,
    onPrimary = OnBackgroundWhite,
    primaryContainer = PrimaryBlue,
    onPrimaryContainer = OnBackgroundWhite,
    
    // Secondary colors
    secondary = PrimaryBlue.copy(alpha = 0.7f),
    onSecondary = OnBackgroundWhite,
    secondaryContainer = SurfaceCard,
    onSecondaryContainer = OnSurfaceVariant,
    
    // Tertiary colors
    tertiary = SuccessGreen,
    onTertiary = BackgroundDark,
    
    // Background colors
    background = BackgroundDark,
    onBackground = OnBackgroundWhite,
    
    // Surface colors
    surface = SurfaceCard,
    onSurface = OnBackgroundWhite,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = OnSurfaceVariant,
    
    // Container colors
    surfaceContainerHighest = SurfaceCard,
    surfaceContainer = SurfaceCard,
    surfaceContainerLow = BackgroundDark,
    
    // Error colors
    error = WarningOrange,
    onError = OnBackgroundWhite,
    
    // Outline
    outline = DividerColor,
    outlineVariant = SurfaceCard
)

@Composable
fun TUBESTheme(
    // Force dark theme always
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}