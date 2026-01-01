package com.example.tubes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tubes.model.Task
import com.example.tubes.ui.theme.*
import com.example.tubes.viewmodel.TaskUiState
import com.example.tubes.viewmodel.TaskViewModel

// Colors for this screen
private val DangerRed = Color(0xFFFF3B30)
private val SafeGreen = Color(0xFF34C759)

/**
 * RecycleBinScreen - Deep Blue Modern Theme
 * 
 * Features:
 * - NavyDeep header with CenterAlignedTopAppBar
 * - BackgroundLight body
 * - White task cards with restore/delete actions
 * - Beautiful empty state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinScreen(
    onNavigateUp: () -> Unit,
    viewModel: TaskViewModel = viewModel()
) {
    val deletedTasksState by viewModel.deletedTasksState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "Recycle Bin",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = NavyDeep
                )
            )
        },
        containerColor = Background
    ) { paddingValues ->
        when (val state = deletedTasksState) {
            is TaskUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NavyDeep)
                }
            }
            
            is TaskUiState.Success -> {
                val deletedTasks = state.tasks
                
                if (deletedTasks.isEmpty()) {
                    // ==================== EMPTY STATE ====================
                    EmptyRecycleBinState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                } else {
                    // ==================== TASK LIST ====================
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header info
                        item {
                            Text(
                                text = "${deletedTasks.size} deleted task${if (deletedTasks.size > 1) "s" else ""}",
                                fontSize = 14.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        items(
                            items = deletedTasks,
                            key = { it.id }
                        ) { task ->
                            TrashedTaskCard(
                                task = task,
                                onRestoreClick = { viewModel.restoreTask(task) },
                                onDeleteForeverClick = { viewModel.deleteTaskPermanently(task.id) }
                            )
                        }
                        
                        // Bottom spacing
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
            
            is TaskUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = state.message,
                            color = DangerRed,
                            fontSize = 14.sp
                        )
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyDeep),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Empty State - Beautiful centered display when bin is empty
 */
@Composable
private fun EmptyRecycleBinState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Large delete icon
            Icon(
                imageVector = Icons.Rounded.DeleteOutline,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = DividerGrey
            )
            
            // Title
            Text(
                text = "No Deleted Tasks",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavyDeep
            )
            
            // Subtitle
            Text(
                text = "Tasks you delete will appear here",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}

/**
 * Trashed Task Card - White card with task info and action buttons
 */
@Composable
private fun TrashedTaskCard(
    task: Task,
    onRestoreClick: () -> Unit,
    onDeleteForeverClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ==================== LEFT: Task Info ====================
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Task Title
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NavyDeep,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Category Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Background
                ) {
                    Text(
                        text = task.category,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                // Due date if available
                if (task.dueDate != null) {
                    Text(
                        text = "📅 ${com.example.tubes.util.DateUtils.formatDateTime(task.dueDate)}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
            
            // ==================== RIGHT: Action Buttons ====================
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Restore Button
                IconButton(
                    onClick = onRestoreClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Restore,
                        contentDescription = "Restore",
                        tint = SafeGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Delete Forever Button
                IconButton(
                    onClick = onDeleteForeverClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteForever,
                        contentDescription = "Delete Forever",
                        tint = DangerRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
