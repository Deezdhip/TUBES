package com.example.tubes.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tubes.model.Task
import com.example.tubes.ui.theme.*
import com.example.tubes.util.DateUtils

// ==================== COLORS ====================

/**
 * Warna aksen untuk pin icon
 */
private val PinActiveColor = Color(0xFFFFD700) // Gold/Yellow

/**
 * Warna untuk deadline yang sudah lewat (overdue)
 */
private val OverdueColor = Color(0xFFFF4444) // Red

// ==================== MAIN COMPONENT ====================

/**
 * Modern Task Item Card dengan Material3 Design.
 * 
 * @param task Task yang akan ditampilkan
 * @param onClick Callback saat card diklik (navigasi ke detail/timer)
 * @param onCheckClick Callback saat checkbox diklik dengan status baru
 * @param onPinClick Callback saat pin icon diklik
 * @param onDeleteClick Callback saat delete icon diklik
 * @param modifier Modifier untuk styling tambahan
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
    // Determine if task is overdue
    val isOverdue = DateUtils.isOverdue(task.dueDate) && !task.isCompleted
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable { onClick(task) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isPinned) Color(0xFF2C2C2C) else TaskCardDark
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (task.isPinned) 6.dp else 2.dp
        ),
        border = if (task.isPinned) {
            BorderStroke(1.dp, PinActiveColor.copy(alpha = 0.5f))
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ==================== CHECKBOX ====================
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { isChecked ->
                    onCheckClick(task, isChecked)
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = SuccessGreen,
                    uncheckedColor = OnSurfaceVariant,
                    checkmarkColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // ==================== PIN BUTTON ====================
            IconButton(
                onClick = { onPinClick(task) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (task.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = if (task.isPinned) "Unpin" else "Pin",
                    tint = if (task.isPinned) PinActiveColor else OnSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // ==================== CONTENT ====================
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    ),
                    color = if (task.isCompleted) OnSurfaceVariant else Color.White,
                    textDecoration = if (task.isCompleted) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // ==================== DEADLINE ====================
                if (task.dueDate != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📅 ${DateUtils.formatDate(task.dueDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = when {
                            isOverdue -> OverdueColor
                            task.isCompleted -> OnSurfaceVariant.copy(alpha = 0.6f)
                            else -> OnSurfaceVariant
                        },
                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ==================== CHIPS (Priority & Category) ====================
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Priority Badge
                    PriorityChip(priority = task.priority)

                    // Category Badge
                    CategoryChip(category = task.category)
                }

                // ==================== COMPLETED STATUS ====================
                if (task.isCompleted) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✓ Completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = SuccessGreen,
                        fontSize = 11.sp
                    )
                }
            }

            // ==================== DELETE BUTTON ====================
            IconButton(
                onClick = { onDeleteClick(task) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Task",
                    tint = WarningOrange.copy(alpha = 0.8f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ==================== HELPER COMPONENTS ====================

/**
 * Chip untuk menampilkan priority task
 */
@Composable
private fun PriorityChip(priority: String) {
    val backgroundColor = when (priority) {
        "High" -> WarningOrange.copy(alpha = 0.8f)
        "Medium" -> PrimaryBlue.copy(alpha = 0.7f)
        else -> SuccessGreen.copy(alpha = 0.7f) // Low
    }

    SuggestionChip(
        onClick = { },
        label = {
            Text(
                text = priority,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = backgroundColor
        ),
        border = null,
        modifier = Modifier.height(24.dp)
    )
}

/**
 * Chip untuk menampilkan category task
 */
@Composable
private fun CategoryChip(category: String) {
    SuggestionChip(
        onClick = { },
        label = {
            Text(
                text = category,
                color = OnSurfaceVariant,
                fontSize = 10.sp
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = SurfaceCard
        ),
        border = BorderStroke(
            width = 1.dp,
            color = OnSurfaceVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier.height(24.dp)
    )
}
