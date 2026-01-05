package com.example.tubes.ui.screens

import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
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

// Premium Design Colors
private val ProgressGold = Color(0xFFFFC107)
private val SuccessGreen = Color(0xFF34C759)

/**
 * TimerScreen - White Lab & Navy Panel Design
 * 
 * Premium minimalist design inspired by Apple Watch Ultra:
 * - Clean white background
 * - Navy floating card with 40dp rounded corners
 * - 20dp elevation for floating effect
 * - Elegant typography with letter spacing
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
    
    // ==================== KEEP SCREEN ON ====================
    DisposableEffect(uiState.timerState) {
        val activity = context as? ComponentActivity
        if (uiState.timerState == TimerState.RUNNING) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    
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
    
    // Handle timer completion - trigger vibration & notification
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

    // ==================== MAIN LAYOUT (WHITE BACKGROUND) ====================
    Scaffold(
        containerColor = Color.White,
        topBar = {
            // Back Button - Navy on white
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = NavyDeep,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        
        // Centered Content
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // ==================== NAVY FLOATING PANEL ====================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(40.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDeep),
                elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp, horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ==================== HEADER ====================
                    // Task Title
                    if (uiState.isLoadingTask) {
                        Text(
                            text = "Loading...",
                            fontSize = 18.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = uiState.taskTitle,
                            fontSize = 18.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // ==================== CIRCULAR TIMER ====================
                    Box(
                        modifier = Modifier.size(250.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Track (background circle)
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.size(250.dp),
                            color = Color.White.copy(alpha = 0.1f),
                            strokeWidth = 10.dp,
                            trackColor = Color.Transparent
                        )
                        
                        // Progress (foreground circle)
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(250.dp),
                            color = when (uiState.timerState) {
                                TimerState.RUNNING -> ProgressGold
                                TimerState.PAUSED -> AccentBlue
                                TimerState.IDLE -> if (uiState.isTimerFinished) SuccessGreen else AccentBlue
                            },
                            strokeWidth = 10.dp,
                            trackColor = Color.Transparent,
                            strokeCap = StrokeCap.Round
                        )
                        
                        // Time Display
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Big Time Text
                            Text(
                                text = formatTime(uiState.timeLeftInSeconds),
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )
                            
                            // State indicator
                            Text(
                                text = when {
                                    uiState.isTimerFinished -> "DONE"
                                    uiState.timerState == TimerState.RUNNING -> "RUNNING"
                                    uiState.timerState == TimerState.PAUSED -> "PAUSED"
                                    else -> "READY"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = when {
                                    uiState.isTimerFinished -> SuccessGreen
                                    uiState.timerState == TimerState.RUNNING -> ProgressGold
                                    else -> Color.White.copy(alpha = 0.6f)
                                },
                                letterSpacing = 2.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // ==================== SET DURATION (only when IDLE) ====================
                    if (uiState.timerState == TimerState.IDLE && !uiState.isTimerFinished) {
                        TextButton(
                            onClick = { showCustomDialog = true },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color.White.copy(alpha = 0.7f)
                            )
                        ) {
                            Icon(
                                Icons.Rounded.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Set Duration",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // ==================== CONTROL BUTTONS ====================
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Stop/Reset Button (only when running/paused and NOT finished)
                        if (uiState.timerState != TimerState.IDLE && !uiState.isTimerFinished) {
                            OutlinedIconButton(
                                onClick = { timerViewModel.resetTimer() },
                                modifier = Modifier.size(56.dp),
                                shape = CircleShape,
                                colors = IconButtonDefaults.outlinedIconButtonColors(
                                    contentColor = Color.White
                                ),
                                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                    brush = SolidColor(Color.White.copy(alpha = 0.3f))
                                )
                            ) {
                                Icon(
                                    Icons.Rounded.Stop,
                                    contentDescription = "Stop",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        // ==================== MAIN BUTTON ====================
                        if (uiState.isTimerFinished) {
                            // ===== FINISH BUTTON (Green) =====
                            Button(
                                onClick = {
                                    // Real-Time Tracking: Complete task with focusTimeSpent
                                    timerViewModel.completeTask {
                                        onNavigateBack()
                                    }
                                },
                                modifier = Modifier.size(80.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SuccessGreen,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(0.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                            ) {
                                if (uiState.isCompletingTask) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        color = Color.White,
                                        strokeWidth = 3.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Complete Task",
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        } else {
                            // ===== PLAY/PAUSE BUTTON (White with Navy icon) =====
                            Button(
                                onClick = { timerViewModel.toggleTimer() },
                                modifier = Modifier.size(80.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = NavyDeep
                                ),
                                contentPadding = PaddingValues(0.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                            ) {
                                Icon(
                                    imageVector = if (uiState.timerState == TimerState.RUNNING) 
                                        Icons.Rounded.Pause 
                                    else 
                                        Icons.Rounded.PlayArrow,
                                    contentDescription = if (uiState.timerState == TimerState.RUNNING) "Pause" else "Start",
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        
                        // Spacer for symmetry
                        if (uiState.timerState != TimerState.IDLE && !uiState.isTimerFinished) {
                            Spacer(modifier = Modifier.size(56.dp))
                        }
                    }
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
