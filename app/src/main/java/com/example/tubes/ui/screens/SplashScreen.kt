package com.example.tubes.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ==================== COLORS ====================
private val NavyDeep = Color(0xFF0D1B3E)
private val AccentBlue = Color(0xFF3B82F6)

/**
 * SplashScreen - Clean Typographic Focus
 * 
 * Minimalist design:
 * - Solid navy background
 * - Blue icon + white bold text
 * - Simple scale+fade entrance
 */
@Composable
fun SplashScreen(
    onNavigateToNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ==================== ENTRANCE ANIMATION ====================
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        // Animate scale: 0.5 -> 1.0
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            )
        )
    }
    
    LaunchedEffect(Unit) {
        // Animate alpha: 0 -> 1
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            )
        )
        
        // Wait after animation
        delay(1500)
        
        // Navigate
        onNavigateToNext()
    }
    
    // ==================== LAYOUT ====================
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NavyDeep),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.graphicsLayer(
                scaleX = scale.value,
                scaleY = scale.value,
                alpha = alpha.value
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Icon(
                imageVector = Icons.Rounded.TaskAlt,
                contentDescription = "Logo",
                tint = AccentBlue,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // App Name
            Text(
                text = "FocusTask",
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}
