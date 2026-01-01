package com.example.tubes.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tubes.ui.theme.*
import com.example.tubes.viewmodel.StatisticsViewModel

/**
 * Modern Project Manager Style - DashboardScreen
 * Deep Blue Theme dengan curved header dan circular progress
 */
@Composable
fun DashboardScreen(
    viewModel: StatisticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Animated progress value
    val animatedProgress by animateFloatAsState(
        targetValue = uiState.completionPercentage / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==================== DEEP BLUE HEADER ====================
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .background(
                            color = NavyDeep,
                            shape = RoundedCornerShape(
                                bottomStart = 32.dp,
                                bottomEnd = 32.dp
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp)
                            .padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Title at top
                        Text(
                            text = "My Progress",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Circular Progress Indicator - Larger & High Contrast
                        Box(
                            modifier = Modifier.size(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Background circle (Track) - White transparent
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.size(180.dp),
                                strokeWidth = 14.dp,
                                color = Color.White.copy(alpha = 0.15f),
                                trackColor = Color.Transparent,
                                strokeCap = StrokeCap.Round
                            )
                            
                            // Progress circle - Cyan/Mint for high contrast
                            CircularProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.size(180.dp),
                                strokeWidth = 14.dp,
                                color = Color(0xFF00E5FF), // Bright Cyan
                                trackColor = Color.Transparent,
                                strokeCap = StrokeCap.Round
                            )
                            
                            // Percentage text in center
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${(animatedProgress * 100).toInt()}%",
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Completed",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Weekly Progress label
                        Text(
                            text = "Weekly Progress",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                        
                        // Bottom spacer to push content away from curved edge
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }
            
            // ==================== STATS CARDS HEADER ====================
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "Overview",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // ==================== STAT CARDS ====================
            // Total Tasks
            item {
                StatCard(
                    icon = Icons.Outlined.Assignment,
                    title = "Total Tasks",
                    value = "${uiState.totalTasks}",
                    iconColor = AccentBlue,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            
            // Completed
            item {
                StatCard(
                    icon = Icons.Outlined.CheckCircle,
                    title = "Completed",
                    value = "${uiState.completedTasks}",
                    iconColor = SuccessGreen,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
            
            // Pending
            item {
                StatCard(
                    icon = Icons.Outlined.Schedule,
                    title = "Pending",
                    value = "${uiState.pendingTasks}",
                    iconColor = WarningOrange,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            
            // Focus Time
            item {
                StatCard(
                    icon = Icons.Outlined.Timer,
                    title = "Focus Time",
                    value = "${uiState.totalFocusMinutes}m",
                    iconColor = CategoryStudy,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
            
            // ==================== CATEGORY BREAKDOWN ====================
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "By Category",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        if (uiState.tasksByCategory.isEmpty()) {
                            Text(
                                text = "No tasks yet",
                                color = TextSecondary,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            uiState.tasksByCategory.forEach { (category, count) ->
                                CategoryRow(
                                    category = category,
                                    count = count,
                                    total = uiState.totalTasks
                                )
                                if (category != uiState.tasksByCategory.keys.last()) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        color = DividerGrey
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Loading overlay
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NavyDeep)
            }
        }
    }
}

/**
 * Stat Card for grid display
 */
@Composable
private fun StatCard(
    icon: ImageVector,
    title: String,
    value: String,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Icon with colored background
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Value - Large
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = NavyDeep
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Label - Small grey
            Text(
                text = title,
                fontSize = 13.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Category row with progress bar
 */
@Composable
private fun CategoryRow(
    category: String,
    count: Int,
    total: Int
) {
    val progress = if (total > 0) count.toFloat() / total else 0f
    val categoryColor = when (category.lowercase()) {
        "work" -> CategoryWork
        "personal" -> CategoryPersonal
        "study" -> CategoryStudy
        else -> CategoryOthers
    }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(categoryColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = category,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDark
                )
            }
            Text(
                text = "$count tasks",
                fontSize = 14.sp,
                color = NavyDeep,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Progress bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = categoryColor,
            trackColor = categoryColor.copy(alpha = 0.2f)
        )
    }
}
