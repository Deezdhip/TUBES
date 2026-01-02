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
import androidx.compose.runtime.*
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

private const val TAG = "TaskItem"

// ==================== WARNA VISUAL ====================
private val PinnedGold = Color(0xFFFFC107)
private val OverdueRed = Color(0xFFFF6B35)
private val CompletedGreen = Color(0xFF34C759)
private val TextWhite = Color.White
private val TextWhiteDim = Color.White.copy(alpha = 0.6f)
private val UncheckedGray = Color.Gray

/**
 * Navy Blue Task Card - STATELESS Component
 * 
 * PENTING: Komponen ini TIDAK memiliki internal state untuk checkbox.
 * Semua visual ditentukan langsung dari parameter `task`.
 * 
 * Features:
 * - NavyDeep background dengan teks putih
 * - Overdue: Deadline teks merah/orange
 * - Pinned: Icon pin emas
 * - Checkbox: Hijau (selesai) / Abu-abu (belum)
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
    // ==================== KEY WRAPPER untuk mencegah state confusion ====================
    // Ini memastikan bahwa remember state di bawah terikat ke task.id yang spesifik
    key(task.id) {
        TaskItemContent(
            task = task,
            onClick = onClick,
            onCheckClick = onCheckClick,
            onPinClick = onPinClick,
            onDeleteClick = onDeleteClick,
            modifier = modifier
        )
    }
}

@Composable
private fun TaskItemContent(
    task: Task,
    onClick: (Task) -> Unit,
    onCheckClick: (Task, Boolean) -> Unit,
    onPinClick: (Task) -> Unit,
    onDeleteClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    // State untuk menu dropdown - HANYA untuk menu, BUKAN untuk checkbox
    var showMenu by remember { mutableStateOf(false) }
    
    // Get category icon and color
    val (categoryIcon, categoryColor) = getCategoryIconAndColor(task.category)
    
    // ==================== LOGIKA VISUAL LANGSUNG DARI DATA ====================
    // KRITIS: Gunakan task.isCompleted langsung, BUKAN variable internal
    val statusIcon: ImageVector = if (task.isCompleted) {
        Icons.Rounded.CheckCircle
    } else {
        Icons.Rounded.RadioButtonUnchecked
    }
    
    val statusColor: Color = if (task.isCompleted) {
        CompletedGreen
    } else {
        UncheckedGray
    }
    
    // Check if overdue (deadline passed AND not completed)
    val isOverdue: Boolean = !task.isCompleted && DateUtils.isOverdue(task.dueDate)
    
    // Warna deadline
    val deadlineColor: Color = if (isOverdue) OverdueRed else TextWhiteDim
    
    // Text decoration untuk judul
    val titleDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
    
    // DEBUG LOG - verifikasi data yang masuk
    Log.d(TAG, "TaskItem: ${task.title} | id=${task.id} | isCompleted=${task.isCompleted} | icon=${if(task.isCompleted) "CHECK" else "UNCHECKED"}")
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(task) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = NavyDeep
        ),
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
                    .background(TextWhite.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = task.category,
                    tint = categoryColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // ==================== TENGAH: Title + Deadline ====================
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Task Title - dengan strikethrough jika selesai
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = titleDecoration
                )
                
                // Deadline Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Clock icon
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = deadlineColor,
                        modifier = Modifier.size(14.dp)
                    )
                    
                    // Deadline text
                    if (task.dueDate != null) {
                        Text(
                            text = DateUtils.formatDate(task.dueDate),
                            fontSize = 12.sp,
                            color = deadlineColor
                        )
                        
                        // Overdue label
                        if (isOverdue) {
                            Text(
                                text = "• Overdue",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = OverdueRed
                            )
                        }
                    } else {
                        Text(
                            text = "No deadline",
                            fontSize = 12.sp,
                            color = TextWhiteDim.copy(alpha = 0.5f)
                        )
                    }
                }
                
                // Category label
                Text(
                    text = task.category,
                    fontSize = 11.sp,
                    color = categoryColor
                )
            }
            
            // ==================== KANAN: Pin + Status Checkbox ====================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Pin Icon (hanya muncul jika pinned)
                if (task.isPinned) {
                    Icon(
                        imageVector = Icons.Rounded.PushPin,
                        contentDescription = "Pinned",
                        tint = PinnedGold,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(-45f)
                    )
                }
                
                // ==================== STATUS CHECKBOX ====================
                // KRITIS: Icon dan warna langsung dari task.isCompleted
                IconButton(
                    onClick = { 
                        // Toggle ke status kebalikan
                        val newStatus = !task.isCompleted
                        Log.d(TAG, "Checkbox clicked: ${task.title} -> newStatus=$newStatus")
                        onCheckClick(task, newStatus)
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = statusIcon,  // Dari logika di atas
                        contentDescription = if (task.isCompleted) "Completed" else "Mark Complete",
                        tint = statusColor,  // Dari logika di atas
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // More Options Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = TextWhiteDim,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        // Pin/Unpin
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    if (task.isPinned) "Unpin" else "Pin to Top",
                                    color = TextDark
                                ) 
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.PushPin,
                                    null,
                                    tint = PinnedGold,
                                    modifier = Modifier.rotate(if (task.isPinned) 0f else -45f)
                                )
                            },
                            onClick = {
                                onPinClick(task)
                                showMenu = false
                            }
                        )
                        
                        HorizontalDivider()
                        
                        // Delete
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFE53935)) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Delete, null, tint = Color(0xFFE53935))
                            },
                            onClick = {
                                onDeleteClick(task)
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
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
        "others" -> Icons.Default.MoreHoriz to CategoryOthers
        else -> Icons.Default.Task to CategoryOthers
    }
}
