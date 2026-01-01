package com.example.tubes.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tubes.ui.theme.NavyDeep
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Deep Blue Background
private val SplashBackground = Color(0xFF0D1B3E)

/**
 * SplashScreen - Deep Blue Modern Theme
 * 
 * Features:
 * - NavyDeep solid background
 * - TaskAlt icon + App name with fade-in & scale-up animation
 * - Auto navigation after 2.5 seconds
 */
@Composable
fun SplashScreen(
    onNavigateToNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation states
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        // Parallel animations: fade in + scale up
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = FastOutSlowInEasing
                )
            )
        }
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = FastOutSlowInEasing
                )
            )
        }
        
        // Total splash duration: 2.5 seconds
        delay(2500)
        
        // Navigate to next screen
        onNavigateToNext()
    }

    // ==================== MAIN LAYOUT ====================
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = SplashBackground),
        contentAlignment = Alignment.Center
    ) {
        // Content Column with animations
        Column(
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale.value,
                    scaleY = scale.value,
                    alpha = alpha.value
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Icon
            Icon(
                imageVector = Icons.Rounded.TaskAlt,
                contentDescription = "FocusTask Logo",
                tint = Color.White,
                modifier = Modifier.size(120.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // App Name
            Text(
                text = "FocusTask",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tagline (optional)
            Text(
                text = "Stay Focused, Get Things Done",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.7f),
                letterSpacing = 0.5.sp
            )
        }
    }
}
