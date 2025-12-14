package com.example.tubes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tubes.model.Task
import com.example.tubes.ui.theme.*

/**
 * Modern task item with dark card background
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(
    task: Task,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onPinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isPinned) Color(0xFF2C2C2C) else TaskCardDark
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (task.isPinned) 8.dp else 2.dp),
        border = if (task.isPinned) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Standard Checkbox
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { 
                    android.util.Log.d("DEBUG_UI", "Checkbox clicked: ${task.title}, ID: ${task.id}")
                    onCheckedChange(it) 
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = PrimaryBlue,
                    uncheckedColor = OnSurfaceVariant,
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Pin Button works independently
            IconButton(
                onClick = {
                    android.util.Log.d("DEBUG_UI", "Pin clicked: ${task.title}, ID: ${task.id}")
                    onPinClick()
                },
                modifier = Modifier.size(48.dp)
            ) {
               Icon(
                   imageVector = if (task.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                   contentDescription = if (task.isPinned) "Unpin" else "Pin",
                   tint = if (task.isPinned) Color(0xFFFFD700) else OnSurfaceVariant,
                   modifier = Modifier.size(20.dp)
               )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Task title area handles the navigation click
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onClick() } 
            ) {
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
                    }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Priority and Category Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Priority Badge
                    SuggestionChip(
                        onClick = { },
                        label = { Text(task.priority, color = OnBackgroundWhite, fontSize = 10.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = when(task.priority) {
                                "High" -> WarningOrange.copy(alpha = 0.8f)
                                "Medium" -> PrimaryBlue.copy(alpha = 0.6f)
                                else -> SuccessGreen.copy(alpha = 0.6f)
                            }
                        ),
                        border = null,
                        modifier = Modifier.height(24.dp)
                    )
                    
                    // Category Badge
                    SuggestionChip(
                        onClick = { },
                        label = { Text(task.category, color = OnSurfaceVariant, fontSize = 10.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = SurfaceCard
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = OnSurfaceVariant.copy(alpha = 0.3f)
                        ),
                         modifier = Modifier.height(24.dp)
                    )
                }

                // Optional: Show completion status
                if (task.isCompleted) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Completed ✓",
                        style = MaterialTheme.typography.bodySmall,
                        color = SuccessGreen,
                        fontSize = 12.sp
                    )
                }
            }

            // Delete button with modern styling
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Task",
                    tint = WarningOrange.copy(alpha = 0.8f)
                )
            }
        }
    }
}
