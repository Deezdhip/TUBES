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
 * Light Color Scheme untuk White Minimalist Theme
 * dengan aksen Royal Blue
 */
private val LightColorScheme = lightColorScheme(
    // Primary colors
    primary = PrimaryBlue,
    onPrimary = SurfaceWhite,
    primaryContainer = PrimaryBlue.copy(alpha = 0.12f),
    onPrimaryContainer = PrimaryVariant,
    
    // Secondary colors (menggunakan variant dari primary)
    secondary = PrimaryVariant,
    onSecondary = SurfaceWhite,
    secondaryContainer = PrimaryBlue.copy(alpha = 0.08f),
    onSecondaryContainer = PrimaryVariant,
    
    // Tertiary colors (menggunakan accent color)
    tertiary = PinGold,
    onTertiary = SurfaceWhite,
    tertiaryContainer = PinGold.copy(alpha = 0.12f),
    onTertiaryContainer = PinGold,
    
    // Background colors
    background = BackgroundLight,
    onBackground = TextPrimary,
    
    // Surface colors
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = TextSecondary,
    
    // Error colors
    error = ErrorRed,
    onError = SurfaceWhite,
    errorContainer = ErrorRed.copy(alpha = 0.12f),
    onErrorContainer = ErrorRed,
    
    // Other colors
    outline = DividerGrey,
    outlineVariant = DividerGrey.copy(alpha = 0.5f),
    scrim = TextPrimary.copy(alpha = 0.32f),
    
    // Inverse colors (untuk snackbar, dll)
    inverseSurface = TextPrimary,
    inverseOnSurface = SurfaceWhite,
    inversePrimary = PrimaryBlue.copy(alpha = 0.8f),
    
    // Surface tint
    surfaceTint = PrimaryBlue
)

/**
 * TUBES Theme - White Minimalist dengan Royal Blue accent
 * 
 * CATATAN: Theme ini SELALU menggunakan LightColorScheme
 * untuk tampilan konsisten putih minimalis.
 * Dynamic color dan dark theme dinonaktifkan.
 */
@Suppress("DEPRECATION")
@Composable
fun TUBESTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Parameter diabaikan
    dynamicColor: Boolean = false, // Dynamic color dinonaktifkan
    content: @Composable () -> Unit
) {
    // SELALU gunakan Light scheme untuk tampilan konsisten
    val colorScheme = LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            // Set status bar color ke background light
            window.statusBarColor = BackgroundLight.toArgb()
            
            // Gunakan dark icons di status bar (karena background terang)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            
            // Set navigation bar juga light
            window.navigationBarColor = BackgroundLight.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}