package com.example.tubes.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Shared Add Task Dialog Component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var taskTitle by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var selectedCategory by remember { mutableStateOf("Personal") }

    val priorities = listOf("Low", "Medium", "High")
    val categories = listOf("Personal", "Work", "Study", "Others")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            Column(
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Priority Selection
                Text("Priority", style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    priorities.forEach { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            label = { Text(priority) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Category Selection
                Text("Category", style = MaterialTheme.typography.labelLarge)
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
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
                        onAdd(taskTitle, selectedPriority, selectedCategory)
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
