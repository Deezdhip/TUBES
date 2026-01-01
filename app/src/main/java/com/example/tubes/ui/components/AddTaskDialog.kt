package com.example.tubes.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.tubes.ui.theme.*
import com.example.tubes.util.DateTimePickerUtils
import com.example.tubes.util.DateUtils

/**
 * Modern Add Task Dialog - Clean Minimalist Card Design
 * Deep Blue Theme dengan White Background
 */
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // ==================== MAIN CARD CONTAINER ====================
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // ==================== HEADER ====================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Task",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDeep
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // ==================== TASK TITLE INPUT ====================
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    placeholder = { 
                        Text(
                            "What needs to be done?",
                            color = TextSecondary
                        ) 
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedBorderColor = NavyDeep,
                        unfocusedBorderColor = DividerGrey,
                        cursorColor = NavyDeep,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // ==================== DEADLINE SECTION ====================
                Text(
                    text = "Deadline",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Deadline Button (Pill Style)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clickable {
                                DateTimePickerUtils.showDateTimePicker(
                                    context = context,
                                    initialTimestamp = selectedDeadline,
                                    onDateTimeSelected = { timestamp ->
                                        selectedDeadline = timestamp
                                    }
                                )
                            },
                        shape = RoundedCornerShape(50),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, DividerGrey)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = "Calendar",
                                tint = if (selectedDeadline != null) NavyDeep else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = selectedDeadline?.let { DateUtils.formatDateTime(it) }
                                    ?: "Set deadline",
                                color = if (selectedDeadline != null) TextDark else TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    // Clear button (only if deadline set)
                    if (selectedDeadline != null) {
                        IconButton(
                            onClick = { selectedDeadline = null },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = ErrorRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // ==================== PRIORITY SECTION ====================
                Text(
                    text = "Priority",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    priorities.forEach { priority ->
                        SelectableChip(
                            text = priority,
                            isSelected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // ==================== CATEGORY SECTION ====================
                Text(
                    text = "Category",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    categories.forEach { category ->
                        SelectableChip(
                            text = category,
                            isSelected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // ==================== ACTION BUTTONS ====================
                // Cancel Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Cancel",
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Create Task Button (Full Width, Solid)
                Button(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            onAdd(taskTitle, selectedPriority, selectedCategory, selectedDeadline)
                        }
                    },
                    enabled = taskTitle.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyDeep,
                        contentColor = Color.White,
                        disabledContainerColor = NavyDeep.copy(alpha = 0.4f),
                        disabledContentColor = Color.White.copy(alpha = 0.6f)
                    )
                ) {
                    Text(
                        text = "Create Task",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Custom Selectable Chip - Clean Minimalist Style
 * Selected: NavyDeep background, White text
 * Unselected: White background, Grey border, Grey text
 */
@Composable
fun SelectableChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(40.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) NavyDeep else Color.White,
        border = if (isSelected) null else BorderStroke(1.dp, DividerGrey)
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isSelected) Color.White else TextSecondary,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}
