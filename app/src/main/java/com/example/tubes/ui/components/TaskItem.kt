package com.example.tubes.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tubes.model.Task
import com.example.tubes.ui.theme.*
import com.example.tubes.util.DateUtils

private const val TAG = "TaskItemDebug"

/**
 * Modern Project Manager Style - Grid Card Task Item
 * 
 * PENTING: Progress membaca nilai ASLI dari task.progress
 * Tidak ada data palsu/hardcoded!
 */
@Composable
fun TaskItem(
    task: Task,
    onClick: (Task) -> Unit,
    onCheckClick: (Task, Boolean) -> Unit,
    onPinClick: (Task) -> Unit,
    onDeleteClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    // Get category icon and color
    val (categoryIcon, categoryColor) = getCategoryIconAndColor(task.category)
    
    // ============================================
    // MEMBACA DATA ASLI DARI DATABASE
    // ============================================
    val progress = safeProgress(task)
    val progressPercent = "${(progress * 100).toInt()}%"
    
    // Cek apakah task selesai
    val isTaskDone = task.isCompleted || progress >= 0.99f
    
    // DEBUG: Log nilai asli
    Log.d(TAG, "Rendering: ${task.title} | DB progress=${task.progress} | safe=$progress | done=$isTaskDone")
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { 
                // Klik Card = Toggle status task
                Log.d(TAG, "Card clicked: ${task.title}")
                onClick(task) 
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = NavyDeep
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // ==================== TOP ROW: DATE + MENU ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date
                Text(
                    text = DateUtils.formatDateOnly(task.dueDate) 
                        ?: DateUtils.formatDateOnly(task.timestamp) 
                        ?: "",
                    fontSize = 11.sp,
                    color = TextOnBlue.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                
                // Menu Button
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = TextOnBlue.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        // Toggle Complete/Incomplete
                        DropdownMenuItem(
                            text = { Text(if (isTaskDone) "Mark Incomplete" else "Mark Complete") },
                            onClick = {
                                Log.d(TAG, "Menu: Toggle status for ${task.title}")
                                onCheckClick(task, !task.isCompleted)
                                showMenu = false
                            }
                        )
                        // Pin/Unpin
                        DropdownMenuItem(
                            text = { Text(if (task.isPinned) "Unpin" else "Pin") },
                            onClick = {
                                onPinClick(task)
                                showMenu = false
                            }
                        )
                        // Delete
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                onDeleteClick(task)
                                showMenu = false
                            }
                        )
                    }
                }
            }
            
            // ==================== CENTER: ICON + TITLE ====================
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(categoryColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = task.category,
                        tint = categoryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Task Title
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnBlue,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                
                // Category
                Text(
                    text = task.category,
                    fontSize = 12.sp,
                    color = TextOnBlue.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Normal
                )
            }
            
            // ==================== BOTTOM: PROGRESS BAR ====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                // Progress label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progress",
                        fontSize = 11.sp,
                        color = TextOnBlue.copy(alpha = 0.6f)
                    )
                    Text(
                        text = progressPercent,
                        fontSize = 11.sp,
                        color = TextOnBlue.copy(alpha = 0.6f)
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Progress Bar - NILAI ASLI dari database!
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (isTaskDone) SuccessGreen else AccentLightBlue,
                    trackColor = TextOnBlue.copy(alpha = 0.2f)
                )
            }
        }
    }
}

/**
 * SAFE PROGRESS CONVERTER
 * Konversi task.progress ke skala 0.0-1.0 dengan aman:
 * - Jika task.isCompleted = true, return 1.0
 * - Jika progress > 1 (skala 0-100), konversi ke 0-1
 * - Jika progress <= 1 (skala 0.0-1.0), gunakan langsung
 */
private fun safeProgress(task: Task): Float {
    // Jika completed, selalu 100%
    if (task.isCompleted) return 1f
    
    val rawProgress = task.progress
    
    return when {
        // Progress dalam skala 0-100
        rawProgress > 1f -> (rawProgress / 100f).coerceIn(0f, 1f)
        // Progress dalam skala 0.0-1.0
        else -> rawProgress.coerceIn(0f, 1f)
    }
}

/**
 * Get category icon and color
 */
private fun getCategoryIconAndColor(category: String): Pair<ImageVector, Color> {
    return when (category.lowercase()) {
        "work" -> Icons.Default.Work to CategoryWork
        "personal" -> Icons.Default.Person to CategoryPersonal
        "study" -> Icons.Default.School to CategoryStudy
        else -> Icons.Default.Computer to CategoryOthers
    }
}
