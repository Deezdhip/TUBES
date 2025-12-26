package com.example.tubes.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tubes.ui.theme.*
import com.example.tubes.util.NotificationHelper
import com.example.tubes.util.SoundManager
import com.example.tubes.viewmodel.TimerState
import com.example.tubes.viewmodel.TimerViewModel

/**
 * TimerScreen - Task Timer untuk fokus mengerjakan task.
 * 
 * @param taskTitle Judul task yang sedang dikerjakan
 * @param modifier Modifier untuk screen
 * @param initialTimeInMinutes Waktu awal dalam menit (default 25)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    taskTitle: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialTimeInMinutes: Int = 25,
    timerViewModel: TimerViewModel = viewModel()
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    
    // Collect UI state dari ViewModel
    val uiState by timerViewModel.uiState.collectAsState()
    
    // Dialog state
    var showCustomDialog by remember { mutableStateOf(false) }
    var showSoundDialog by remember { mutableStateOf(false) }
    
    // Initialize timer duration saat pertama kali
    LaunchedEffect(initialTimeInMinutes) {
        if (uiState.timerState == TimerState.IDLE) {
            timerViewModel.setDuration(initialTimeInMinutes)
        }
    }
    
    // Handle timer completion - VIBRATE, SOUND, dan NOTIFICATION
    LaunchedEffect(uiState.timerCompleted) {
        if (uiState.timerCompleted) {
            // 1. Stop ambient sound
            soundManager.stopSound()
            
            // 2. Trigger completion feedback (vibrate + notification + sound)
            timerViewModel.triggerCompletionFeedback(context, taskTitle)
            
            // 3. Clear flag agar tidak trigger lagi
            timerViewModel.clearCompletionFlag()
        }
    }
    
    // Manage Ambient Sound Playback
    LaunchedEffect(uiState.timerState, uiState.selectedSound) {
        if (uiState.timerState == TimerState.RUNNING && uiState.selectedSound != "Off") {
            // In a real app, map "Rain" to R.raw.rain, etc.
            // Since we don't have assets, we use a placeholder
            soundManager.playSound(if (uiState.selectedSound == "Rain") 1 else 0) 
        } else {
            soundManager.stopSound()
        }
    }
    
    // Clean up sound dan resources on exit
    DisposableEffect(Unit) {
        onDispose {
            soundManager.stopSound()
            // Timer cleanup handled by ViewModel.onCleared()
        }
    }
    
    // Hitung progress (1.0 = penuh, 0.0 = habis)
    val progress by animateFloatAsState(
        targetValue = if (uiState.totalTimeInSeconds > 0) {
            uiState.timeLeftInSeconds.toFloat() / uiState.totalTimeInSeconds.toFloat()
        } else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "progress"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Timer", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = OnBackgroundWhite
                        )
                    }
                },
                actions = {
                    // Sound Button
                    IconButton(onClick = { showSoundDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Ambient Sound",
                            tint = if (uiState.selectedSound == "Off") OnSurfaceVariant else PrimaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = OnBackgroundWhite
                )
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background Decoration
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = PrimaryBlue.copy(alpha = 0.15f),
                    radius = 250.dp.toPx(),
                    center = center.copy(x = size.width, y = 0f)
                )
                drawCircle(
                    color = PrimaryBlue.copy(alpha = 0.1f),
                    radius = 200.dp.toPx(),
                    center = center.copy(x = 0f, y = size.height)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Judul Task
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = "Sedang mengerjakan:",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = taskTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    if (uiState.selectedSound != "Off") {
                        Spacer(modifier = Modifier.height(8.dp))
                        SuggestionChip(
                            onClick = { showSoundDialog = true },
                            label = { Text("♫ ${uiState.selectedSound}") },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = SurfaceCard)
                        )
                    }
                }
                
                // Duration Selection
                if (uiState.timerState == TimerState.IDLE) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        OutlinedButton(
                            onClick = { showCustomDialog = true },
                            modifier = Modifier.padding(top = 16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PrimaryBlue
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue)
                        ) {
                            Text("Set Custom Duration")
                        }
                    }
                } else {
                     // Spacing placeholder to keep layout stable
                     Spacer(modifier = Modifier.height(64.dp))
                }

                // Circular Progress Indicator dengan Timer
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(300.dp)
                        .padding(16.dp)
                ) {
                    // Background circle
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = SurfaceCard,
                        strokeWidth = 16.dp,
                    )
                    
                    // Progress circle
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = when (uiState.timerState) {
                            TimerState.RUNNING -> PrimaryBlue
                            TimerState.PAUSED -> WarningOrange
                            TimerState.IDLE -> SuccessGreen
                        },
                        strokeWidth = 16.dp,
                        trackColor = SurfaceCard,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )

                    // Timer text di tengah
                    Text(
                        text = formatTime(uiState.timeLeftInSeconds),
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnBackgroundWhite
                    )
                }

                // Control Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    // Start/Pause Button
                    Button(
                        onClick = { timerViewModel.toggleTimer() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = Color.White
                        )
                    ) {
                        if (uiState.timerState != TimerState.RUNNING) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Start",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = when (uiState.timerState) {
                                TimerState.RUNNING -> "Pause"
                                else -> "Start"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Stop/Reset Button
                    OutlinedButton(
                        onClick = { timerViewModel.resetTimer() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = uiState.timerState != TimerState.IDLE,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = WarningOrange
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarningOrange)
                    ) {
                        Text(
                            text = "Reset",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    
    // Custom Duration Dialog
    if (showCustomDialog) {
        var customMinutes by remember { mutableStateOf("") }
        var customSeconds by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("Set Custom Time", color = OnBackgroundWhite) },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = customMinutes,
                        onValueChange = { if (it.all { char -> char.isDigit() }) customMinutes = it },
                        label = { Text("Min") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = OnSurfaceVariant,
                            focusedTextColor = OnBackgroundWhite,
                            unfocusedTextColor = OnBackgroundWhite
                        )
                    )
                    OutlinedTextField(
                        value = customSeconds,
                        onValueChange = { if (it.all { char -> char.isDigit() }) customSeconds = it },
                        label = { Text("Sec") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = OnSurfaceVariant,
                            focusedTextColor = OnBackgroundWhite,
                            unfocusedTextColor = OnBackgroundWhite
                        )
                    )
                }
            },
            containerColor = SurfaceCard,
            confirmButton = {
                TextButton(onClick = {
                    val mins = customMinutes.toIntOrNull() ?: 0
                    val secs = customSeconds.toIntOrNull() ?: 0
                    val totalSecs = (mins * 60) + secs
                    
                    if (totalSecs > 0) {
                        timerViewModel.setDurationInSeconds(totalSecs)
                    }
                    showCustomDialog = false
                }) {
                    Text("Set", color = PrimaryBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) {
                    Text("Cancel", color = OnSurfaceVariant)
                }
            }
        )
    }
    
    // Ambient Sound Dialog
    if (showSoundDialog) {
        val sounds = listOf("Off", "Rain", "Cafe", "White Noise")
        AlertDialog(
            onDismissRequest = { showSoundDialog = false },
            title = { Text("Select Ambient Sound", color = OnBackgroundWhite) },
            text = {
                Column {
                    sounds.forEach { sound ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    timerViewModel.setSelectedSound(sound)
                                    showSoundDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                             RadioButton(
                                selected = (sound == uiState.selectedSound),
                                onClick = { 
                                    timerViewModel.setSelectedSound(sound)
                                    showSoundDialog = false 
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                             )
                             Spacer(modifier = Modifier.width(8.dp))
                             Text(text = sound, color = OnSurfaceVariant, fontSize = 16.sp)
                        }
                    }
                }
            },
            containerColor = SurfaceCard,
            confirmButton = {
                TextButton(onClick = { showSoundDialog = false }) {
                    Text("Close", color = PrimaryBlue)
                }
            }
        )
    }
}

/**
 * Format waktu dari detik ke MM:SS
 */
private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
