package com.example.tubes.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tubes.ui.theme.*
import com.example.tubes.viewmodel.StatisticsViewModel

/**
 * DashboardScreen - Stacked Insight Cards Design
 * 
 * Professional analytics dashboard with:
 * - Hero Metric Card (Focus Time)
 * - Productivity Rate Card
 * - Category Breakdown Card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: StatisticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    
    // Animated progress
    val animatedProgress by animateFloatAsState(
        targetValue = uiState.completionPercentage / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    // Format focus time
    val focusHours = uiState.totalFocusMinutes / 60
    val focusMinutes = uiState.totalFocusMinutes % 60
    val focusTimeText = if (focusHours > 0) "${focusHours}h ${focusMinutes}m" else "${focusMinutes}m"

    Scaffold(
        containerColor = Background
    ) { paddingValues ->
        
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ==================== HEADER ====================
                item {
                    Text(
                        text = "Dashboard",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDeep,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                // ==================== CARD 1: HERO METRIC (Total Focus) ====================
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(NavyDeep, AccentBlue)
                                    ),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .padding(24.dp)
                        ) {
                            // Decorative icon
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.1f),
                                modifier = Modifier
                                    .size(120.dp)
                                    .align(Alignment.CenterEnd)
                            )
                            
                            Column {
                                Text(
                                    text = "Total Focus Time",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Medium
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = focusTimeText,
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = "${uiState.completedTasks} tasks completed",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
                
                // ==================== CARD 2: PRODUCTIVITY RATE ====================
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Completion Rate",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NavyDeep
                                )
                                
                                Text(
                                    text = "${(animatedProgress * 100).toInt()}%",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Progress bar
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = SuccessGreen,
                                trackColor = SuccessGreen.copy(alpha = 0.2f)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Stats row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem(
                                    label = "Completed",
                                    value = "${uiState.completedTasks}",
                                    color = SuccessGreen
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(40.dp)
                                        .background(DividerGrey)
                                )
                                
                                StatItem(
                                    label = "Pending",
                                    value = "${uiState.pendingTasks}",
                                    color = WarningOrange
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(40.dp)
                                        .background(DividerGrey)
                                )
                                
                                StatItem(
                                    label = "Total",
                                    value = "${uiState.totalTasks}",
                                    color = NavyDeep
                                )
                            }
                        }
                    }
                }
                
                // ==================== CARD 3: CATEGORY BREAKDOWN ====================
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Focus by Category",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NavyDeep
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            if (uiState.tasksByCategory.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No tasks yet",
                                        color = TextSecondary,
                                        fontSize = 14.sp
                                    )
                                }
                            } else {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    uiState.tasksByCategory.forEach { (category, count) ->
                                        CategoryBreakdownItem(
                                            category = category,
                                            count = count,
                                            total = uiState.totalTasks
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // ==================== CARD 4: OVERDUE ALERT (if any) ====================
                if (uiState.overdueTasks > 0) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = ErrorRed.copy(alpha = 0.1f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(ErrorRed.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Warning,
                                        contentDescription = null,
                                        tint = ErrorRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Overdue Tasks",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ErrorRed
                                    )
                                    Text(
                                        text = "You have ${uiState.overdueTasks} task${if (uiState.overdueTasks > 1) "s" else ""} past deadline",
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(80.dp))
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
 * Stat item for productivity card
 */
@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Category breakdown item
 */
@Composable
private fun CategoryBreakdownItem(
    category: String,
    count: Int,
    total: Int
) {
    val progress = if (total > 0) count.toFloat() / total else 0f
    val (categoryColor, categoryIcon) = getCategoryStyle(category)
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category icon with background
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(categoryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = category,
                        tint = categoryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Text(
                    text = category,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDark
                )
            }
            
            Text(
                text = "$count task${if (count > 1) "s" else ""}",
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

/**
 * Get category color and icon
 */
private fun getCategoryStyle(categoryName: String): Pair<Color, ImageVector> {
    return when (categoryName.lowercase()) {
        "work" -> Pair(CategoryWork, Icons.Rounded.Work)
        "personal" -> Pair(CategoryPersonal, Icons.Rounded.Person)
        "study" -> Pair(CategoryStudy, Icons.Rounded.School)
        "others" -> Pair(CategoryOthers, Icons.Rounded.MoreHoriz)
        else -> Pair(CategoryOthers, Icons.Rounded.Task)
    }
}
