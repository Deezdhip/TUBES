package com.example.tubes.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tubes.model.Task
import com.example.tubes.ui.theme.*
import com.example.tubes.util.DateUtils

/**
 * 100% STATELESS Task Card
 * 
 * TIDAK ADA: var, remember, mutableStateOf
 * Logika isDone sinkron dengan Dashboard (isCompleted OR progress >= 0.99)
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
    // ==================== LOGIKA FALLBACK SINKRON DENGAN DASHBOARD ====================
    // Checklist menyala HIJAU jika: isCompleted == true ATAU progress >= 0.99
    val isDone = task.isCompleted || task.progress >= 0.99f
    
    // DEBUG: Verifikasi data masuk
    Log.d("TaskItem", "RENDER: ${task.title} | isCompleted=${task.isCompleted} | progress=${task.progress} | isDone=$isDone")
    
    // Visual dari isDone (bukan task.isCompleted langsung)
    val statusIcon = if (isDone) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked
    val statusColor = if (isDone) Color(0xFF34C759) else Color.Gray
    val titleDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(task) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyDeep),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ==================== KIRI: Category Icon ====================
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(task.category),
                    contentDescription = task.category,
                    tint = getCategoryColor(task.category),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // ==================== TENGAH: Title + Deadline ====================
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Title - gunakan isDone
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = titleDecoration
                )
                
                // Deadline Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isOverdue = !isDone && DateUtils.isOverdue(task.dueDate)
                    val deadlineColor = if (isOverdue) Color(0xFFFF6B35) else Color.White.copy(alpha = 0.6f)
                    
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = deadlineColor,
                        modifier = Modifier.size(14.dp)
                    )
                    
                    Text(
                        text = if (task.dueDate != null) DateUtils.formatDate(task.dueDate) else "No deadline",
                        fontSize = 12.sp,
                        color = deadlineColor
                    )
                    
                    if (isOverdue) {
                        Text(
                            text = "• Overdue",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFF6B35)
                        )
                    }
                }
                
                // Category
                Text(
                    text = task.category,
                    fontSize = 11.sp,
                    color = getCategoryColor(task.category)
                )
            }
            
            // ==================== KANAN: Pin + Checkbox ====================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Pin
                if (task.isPinned) {
                    Icon(
                        imageVector = Icons.Rounded.PushPin,
                        contentDescription = "Pinned",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp).rotate(-45f)
                    )
                }
                
                // ==================== CHECKBOX ====================
                // Kirim kebalikan dari isDone (bukan task.isCompleted)
                IconButton(
                    onClick = { onCheckClick(task, !isDone) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = if (isDone) "Completed" else "Mark Complete",
                        tint = statusColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Delete button
                IconButton(
                    onClick = { onDeleteClick(task) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// Helper functions
private fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "work" -> Icons.Default.Work
        "personal" -> Icons.Default.Person
        "study" -> Icons.Default.School
        "others" -> Icons.Default.MoreHoriz
        else -> Icons.Default.Task
    }
}

private fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "work" -> CategoryWork
        "personal" -> CategoryPersonal
        "study" -> CategoryStudy
        "others" -> CategoryOthers
        else -> CategoryOthers
    }
}
