package com.example.tubes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tubes.model.Task
import com.example.tubes.ui.theme.*
import com.example.tubes.util.DateUtils

/**
 * Clean Minimalist Task Item Card
 * White card dengan Royal Blue accents
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
    val isOverdue = DateUtils.isOverdue(task.dueDate) && !task.isCompleted
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .shadow(
                elevation = if (task.isPinned) 6.dp else 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.05f)
            )
            .clickable { onClick(task) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceWhite
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ==================== CIRCULAR CHECKBOX ====================
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (task.isCompleted) SuccessGreen 
                        else Color.Transparent
                    )
                    .then(
                        if (!task.isCompleted) {
                            Modifier.background(
                                color = Color.Transparent,
                                shape = CircleShape
                            )
                        } else Modifier
                    )
                    .clickable { onCheckClick(task, !task.isCompleted) },
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    // Checkmark
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    // Empty circle border
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = DividerGrey,
                                shape = CircleShape
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center)
                                .background(
                                    color = SurfaceWhite,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // ==================== CONTENT ====================
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title - Larger, cleaner
                Text(
                    text = task.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) TextSecondary else TextPrimary,
                    textDecoration = if (task.isCompleted) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Deadline - Below title, small grey text
                if (task.dueDate != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Priority indicator dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = when (task.priority) {
                                        "High" -> ErrorRed
                                        "Medium" -> PrimaryBlue
                                        else -> SuccessGreen
                                    },
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = DateUtils.formatDateTime(task.dueDate),
                            fontSize = 13.sp,
                            color = if (isOverdue) ErrorRed else TextSecondary,
                            fontWeight = if (isOverdue) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                } else {
                    // Show priority dot even without deadline
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = when (task.priority) {
                                        "High" -> ErrorRed
                                        "Medium" -> PrimaryBlue
                                        else -> SuccessGreen
                                    },
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = task.priority,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // ==================== PIN BUTTON (ALWAYS VISIBLE) ====================
            IconButton(
                onClick = { onPinClick(task) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (task.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = if (task.isPinned) "Unpin" else "Pin",
                    tint = if (task.isPinned) PinGold else DisabledGrey,
                    modifier = Modifier.size(20.dp)
                )
            }

            // ==================== DELETE BUTTON ====================
            IconButton(
                onClick = { onDeleteClick(task) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Task",
                    tint = DisabledGrey, // Light grey, non-distracting
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
