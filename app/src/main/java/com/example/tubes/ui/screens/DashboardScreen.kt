package com.example.tubes.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tubes.ui.theme.*
import com.example.tubes.viewmodel.StatisticsViewModel

@Composable
fun DashboardScreen(
    viewModel: StatisticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Background Decoration
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = PrimaryBlue.copy(alpha = 0.15f),
                radius = 300.dp.toPx(),
                center = center.copy(x = size.width, y = 0f)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            item {
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Stats Cards
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    DashboardStatCard(
                        title = "Completed",
                        value = "${uiState.completedTasks}",
                        modifier = Modifier.weight(1f),
                        color = SuccessGreen
                    )
                    DashboardStatCard(
                        title = "Total Tasks",
                        value = "${uiState.totalTasks}",
                        modifier = Modifier.weight(1f),
                        color = PrimaryBlue
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                DashboardStatCard(
                    title = "Estimated Focus Time",
                    value = "${uiState.totalFocusMinutes} min",
                    modifier = Modifier.fillMaxWidth(),
                    color = WarningOrange
                )
            }

            // Category Breakdown
            item {
                Text(
                    text = "By Category",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        uiState.tasksByCategory.forEach { (category, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(category, color = OnBackgroundWhite)
                                Text("$count tasks", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                            }
                            if (category != uiState.tasksByCategory.keys.last()) {
                                HorizontalDivider(color = OnSurfaceVariant.copy(alpha = 0.2f))
                            }
                        }
                        if (uiState.tasksByCategory.isEmpty()) {
                            Text("No tasks yet", color = OnSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
