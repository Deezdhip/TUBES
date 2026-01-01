package com.example.tubes.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Modern Project Manager - Deep Blue Theme Color Scheme
 */
private val LightColorScheme = lightColorScheme(
    // Primary colors - NavyDeep
    primary = NavyDeep,
    onPrimary = TextOnBlue,
    primaryContainer = NavyDeep.copy(alpha = 0.12f),
    onPrimaryContainer = NavyDeep,
    
    // Secondary colors - AccentBlue
    secondary = AccentBlue,
    onSecondary = TextOnBlue,
    secondaryContainer = AccentBlue.copy(alpha = 0.12f),
    onSecondaryContainer = AccentBlue,
    
    // Tertiary colors - Accent
    tertiary = PinGold,
    onTertiary = TextOnBlue,
    tertiaryContainer = PinGold.copy(alpha = 0.12f),
    onTertiaryContainer = PinGold,
    
    // Background colors
    background = Background,
    onBackground = TextDark,
    
    // Surface colors
    surface = SurfaceWhite,
    onSurface = TextDark,
    surfaceVariant = Background,
    onSurfaceVariant = TextSecondary,
    
    // Error colors
    error = ErrorRed,
    onError = TextOnBlue,
    errorContainer = ErrorRed.copy(alpha = 0.12f),
    onErrorContainer = ErrorRed,
    
    // Other colors
    outline = DividerGrey,
    outlineVariant = DividerGrey.copy(alpha = 0.5f),
    scrim = TextDark.copy(alpha = 0.32f),
    
    // Inverse colors
    inverseSurface = TextDark,
    inverseOnSurface = TextOnBlue,
    inversePrimary = AccentBlue,
    
    // Surface tint
    surfaceTint = NavyDeep
)

/**
 * TUBES Theme - Modern Project Manager dengan Deep Blue Theme
 */
@Suppress("DEPRECATION")
@Composable
fun TUBESTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            // Set status bar to Background color
            window.statusBarColor = Background.toArgb()
            
            // Use dark icons on light status bar
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            
            // Navigation bar also light
            window.navigationBarColor = SurfaceWhite.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}