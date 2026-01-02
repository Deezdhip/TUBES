package com.example.tubes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tubes.model.Task
import com.example.tubes.ui.theme.*
import com.example.tubes.util.DateUtils
import com.example.tubes.viewmodel.TaskUiState
import com.example.tubes.viewmodel.TaskViewModel

// Category colors for icons
private val CategoryWorkColor = Color(0xFF4A90D9)
private val CategoryPersonalColor = Color(0xFFE91E63)
private val CategoryStudyColor = Color(0xFF9C27B0)
private val CategoryOthersColor = Color(0xFF607D8B)
private val CompletedGreen = Color(0xFF34C759)

/**
 * CategoryTasksScreen - Deep Blue Modern Theme
 * Displays tasks filtered by category using SimpleTaskRow
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTasksScreen(
    categoryName: String,
    onNavigateBack: () -> Unit,
    onNavigateToTimer: (String) -> Unit = {},
    viewModel: TaskViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "$categoryName Tasks",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
        
        when (uiState) {
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
                val allTasks = (uiState as TaskUiState.Success).tasks
                
                // Filter tasks by category (case-insensitive comparison)
                val categoryTasks = allTasks.filter { task ->
                    task.category.equals(categoryName, ignoreCase = true)
                }
                
                if (categoryTasks.isEmpty()) {
                    // ==================== EMPTY STATE ====================
                    EmptyCategoryState(categoryName = categoryName)
                } else {
                    // ==================== TASK LIST ====================
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Task count header
                        item {
                            Text(
                                text = "${categoryTasks.size} task${if (categoryTasks.size > 1) "s" else ""} found",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        
                        items(
                            items = categoryTasks,
                            key = { it.id }
                        ) { task ->
                            SimpleTaskRow(
                                task = task,
                                categoryName = categoryName,
                                onClick = { onNavigateToTimer(task.id) }
                            )
                        }
                        
                        // Bottom spacing
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
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
                    Text(
                        text = "Failed to load tasks",
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

/**
 * SimpleTaskRow - Minimalist row component for category task list
 * 
 * Layout: [Icon] [Title + Deadline] [Status Arrow/Check]
 */
@Composable
private fun SimpleTaskRow(
    task: Task,
    categoryName: String,
    onClick: () -> Unit
) {
    // Get category color and icon
    val (categoryColor, categoryIcon) = getCategoryStyle(categoryName)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ==================== LEFT: Category Icon ====================
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(categoryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = categoryName,
                    tint = categoryColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            // ==================== CENTER: Title + Deadline ====================
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Task Title
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NavyDeep,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Deadline (if available)
                if (task.dueDate != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = DateUtils.formatDate(task.dueDate),
                            fontSize = 12.sp,
                            color = if (DateUtils.isOverdue(task.dueDate) && !task.isCompleted) 
                                ErrorRed else TextSecondary
                        )
                    }
                } else {
                    Text(
                        text = "No deadline",
                        fontSize = 12.sp,
                        color = TextSecondary.copy(alpha = 0.6f)
                    )
                }
            }
            
            // ==================== RIGHT: Status Icon ====================
            if (task.isCompleted) {
                // Completed: Green check
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Completed",
                    tint = CompletedGreen,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                // Not completed: Chevron (clickable indicator)
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "Open",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Get category color and icon based on category name
 */
private fun getCategoryStyle(categoryName: String): Pair<Color, ImageVector> {
    return when (categoryName.lowercase()) {
        "work" -> Pair(CategoryWorkColor, Icons.Rounded.Work)
        "personal" -> Pair(CategoryPersonalColor, Icons.Rounded.Person)
        "study" -> Pair(CategoryStudyColor, Icons.Rounded.School)
        "others" -> Pair(CategoryOthersColor, Icons.Rounded.MoreHoriz)
        else -> Pair(CategoryOthersColor, Icons.Rounded.Task)
    }
}

/**
 * Empty state when no tasks in category
 */
@Composable
private fun EmptyCategoryState(categoryName: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = DividerGrey
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No $categoryName Tasks",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavyDeep
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Tasks with \"$categoryName\" category will appear here",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
