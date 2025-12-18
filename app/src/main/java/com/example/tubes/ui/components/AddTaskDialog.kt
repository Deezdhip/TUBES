package com.example.tubes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tubes.util.DateTimePickerUtils
import com.example.tubes.util.DateUtils

/**
 * Shared Add Task Dialog Component dengan fitur Deadline
 * 
 * @param onDismiss Callback saat dialog ditutup
 * @param onAdd Callback saat task ditambahkan dengan parameter (title, priority, category, dueDate)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, Long?) -> Unit
) {
    val context = LocalContext.current
    
    var taskTitle by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var selectedCategory by remember { mutableStateOf("Personal") }
    var selectedDeadline by remember { mutableStateOf<Long?>(null) }

    val priorities = listOf("Low", "Medium", "High")
    val categories = listOf("Personal", "Work", "Study", "Others")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Task Title Input
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // ==================== DEADLINE SECTION ====================
                Text("Deadline", style = MaterialTheme.typography.labelLarge)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Set Deadline Button
                    OutlinedButton(
                        onClick = {
                            DateTimePickerUtils.showDateTimePicker(
                                context = context,
                                initialTimestamp = selectedDeadline,
                                onDateTimeSelected = { timestamp ->
                                    selectedDeadline = timestamp
                                }
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Set Deadline",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = selectedDeadline?.let { DateUtils.formatDateTime(it) }
                                ?: "Set Deadline"
                        )
                    }
                    
                    // Clear Deadline Button (only show if deadline is set)
                    if (selectedDeadline != null) {
                        IconButton(
                            onClick = { selectedDeadline = null }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Deadline",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // ==================== PRIORITY SECTION ====================
                Text("Priority", style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    priorities.forEach { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            label = { Text(priority) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (priority) {
                                    "High" -> Color(0xFFFF6B6B)
                                    "Medium" -> Color(0xFF4DABF7)
                                    else -> Color(0xFF51CF66)
                                }
                            )
                        )
                    }
                }

                // ==================== CATEGORY SECTION ====================
                Text("Category", style = MaterialTheme.typography.labelLarge)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories.size) { index ->
                        val category = categories[index]
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (taskTitle.isNotBlank()) {
                        onAdd(taskTitle, selectedPriority, selectedCategory, selectedDeadline)
                    }
                },
                enabled = taskTitle.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
