package com.example.tubes.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tubes.ui.theme.*
import com.example.tubes.util.SoundManager
import com.example.tubes.viewmodel.TimerState
import com.example.tubes.viewmodel.TimerViewModel

// Cyan/Mint accent color for timer progress
private val TimerCyan = Color(0xFF00E5FF)
private val TimerTrack = Color(0xFFE8EDF2)

/**
 * TimerScreen - Deep Blue Modern Split Layout Design
 * 
 * Layout:
 * - Header (40%): NavyDeep curved background with task title
 * - Center: Overlapping circular timer
 * - Bottom: Light background with controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialTimeInMinutes: Int = 25,
    timerViewModel: TimerViewModel = viewModel()
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    
    // Collect UI state
    val uiState by timerViewModel.uiState.collectAsState()
    
    // Dialog state
    var showCustomDialog by remember { mutableStateOf(false) }
    
    // Load task data
    LaunchedEffect(taskId) {
        timerViewModel.loadTask(taskId)
    }
    
    // Initialize timer duration
    LaunchedEffect(initialTimeInMinutes) {
        if (uiState.timerState == TimerState.IDLE) {
            timerViewModel.setDuration(initialTimeInMinutes)
        }
    }
    
    // Handle timer completion
    LaunchedEffect(uiState.timerCompleted) {
        if (uiState.timerCompleted) {
            soundManager.stopSound()
            timerViewModel.triggerCompletionFeedback(context, uiState.taskTitle)
            timerViewModel.clearCompletionFlag()
        }
    }
    
    // Clean up on exit
    DisposableEffect(Unit) {
        onDispose { soundManager.stopSound() }
    }
    
    // Animate progress
    val progress by animateFloatAsState(
        targetValue = if (uiState.totalTimeInSeconds > 0) {
            uiState.timeLeftInSeconds.toFloat() / uiState.totalTimeInSeconds.toFloat()
        } else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "progress"
    )

    // ==================== MAIN LAYOUT ====================
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Background) // Light background for bottom area
    ) {
        // ==================== HEADER (NavyDeep - 40%) ====================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .background(
                    color = NavyDeep,
                    shape = RoundedCornerShape(
                        bottomStart = 32.dp,
                        bottomEnd = 32.dp
                    )
                )
        ) {
            // Back Button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Header Content - Centered
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // "Focusing on" label
                Text(
                    text = "Focusing on",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Normal
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Task Title (from ViewModel)
                if (uiState.isLoadingTask) {
                    Text(
                        text = "Loading...",
                        fontSize = 24.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = uiState.taskTitle,
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                    
                    // Category chip if available
                    if (uiState.taskCategory.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = uiState.taskCategory,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // ==================== TIMER CIRCLE (Overlap Center) ====================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .offset(y = 20.dp), // Slight offset for overlap effect
            contentAlignment = Alignment.Center
        ) {
            // Timer Card with shadow
            Card(
                modifier = Modifier
                    .size(240.dp)
                    .shadow(
                        elevation = 24.dp,
                        shape = CircleShape,
                        ambientColor = NavyDeep.copy(alpha = 0.3f)
                    ),
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Track (background circle)
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(220.dp),
                        color = TimerTrack,
                        strokeWidth = 12.dp
                    )
                    
                    // Progress (foreground circle with Cyan color)
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(220.dp),
                        color = when (uiState.timerState) {
                            TimerState.RUNNING -> TimerCyan
                            TimerState.PAUSED -> AccentBlue
                            TimerState.IDLE -> SuccessGreen
                        },
                        strokeWidth = 12.dp,
                        trackColor = Color.Transparent,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    
                    // Timer Text
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = formatTime(uiState.timeLeftInSeconds),
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyDeep,
                            letterSpacing = 2.sp
                        )
                        
                        // State label
                        Text(
                            text = when (uiState.timerState) {
                                TimerState.RUNNING -> "Running"
                                TimerState.PAUSED -> "Paused"
                                TimerState.IDLE -> "Ready"
                            },
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        // ==================== BOTTOM CONTROLS ====================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Set Duration Chip (only when IDLE)
            if (uiState.timerState == TimerState.IDLE) {
                AssistChip(
                    onClick = { showCustomDialog = true },
                    label = { 
                        Text(
                            "Set Duration",
                            fontWeight = FontWeight.Medium
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = SurfaceWhite,
                        labelColor = NavyDeep,
                        leadingIconContentColor = NavyDeep
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        enabled = true,
                        borderColor = DividerGrey
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Control Buttons Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button (small)
                if (uiState.timerState != TimerState.IDLE) {
                    FloatingActionButton(
                        onClick = { timerViewModel.resetTimer() },
                        modifier = Modifier.size(48.dp),
                        containerColor = SurfaceWhite,
                        contentColor = ErrorRed,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Reset",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // Play/Pause Button (large FAB)
                FloatingActionButton(
                    onClick = { timerViewModel.toggleTimer() },
                    modifier = Modifier.size(72.dp),
                    containerColor = NavyDeep,
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Icon(
                        imageVector = if (uiState.timerState == TimerState.RUNNING) 
                            Icons.Default.Pause 
                        else 
                            Icons.Default.PlayArrow,
                        contentDescription = if (uiState.timerState == TimerState.RUNNING) "Pause" else "Start",
                        modifier = Modifier.size(36.dp)
                    )
                }
                
                // Spacer for symmetry when reset is shown
                if (uiState.timerState != TimerState.IDLE) {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }
    }
    
    // ==================== CUSTOM DURATION DIALOG ====================
    if (showCustomDialog) {
        var customMinutes by remember { mutableStateOf("") }
        var customSeconds by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { 
                Text(
                    "Set Timer Duration",
                    fontWeight = FontWeight.Bold,
                    color = NavyDeep
                ) 
            },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = customMinutes,
                        onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 3) customMinutes = it },
                        label = { Text("Minutes") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyDeep,
                            unfocusedBorderColor = DividerGrey,
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark
                        )
                    )
                    OutlinedTextField(
                        value = customSeconds,
                        onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 2) customSeconds = it },
                        label = { Text("Seconds") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyDeep,
                            unfocusedBorderColor = DividerGrey,
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark
                        )
                    )
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(24.dp),
            confirmButton = {
                Button(
                    onClick = {
                        val mins = customMinutes.toIntOrNull() ?: 0
                        val secs = customSeconds.toIntOrNull() ?: 0
                        val totalSecs = (mins * 60) + secs
                        if (totalSecs > 0) {
                            timerViewModel.setDurationInSeconds(totalSecs)
                        }
                        showCustomDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyDeep),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Set", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

/**
 * Format seconds to MM:SS
 */
private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
