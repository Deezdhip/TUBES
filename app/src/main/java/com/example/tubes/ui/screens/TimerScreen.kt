package com.example.tubes.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Enum untuk state timer
 */
enum class TimerState {
    IDLE, RUNNING, PAUSED
}

/**
 * TimerScreen - Pomodoro timer untuk fokus mengerjakan task.
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
    initialTimeInMinutes: Int = 25
) {
    // State untuk timer
    var timerState by remember { mutableStateOf(TimerState.IDLE) }
    var timeLeftInSeconds by remember { mutableIntStateOf(initialTimeInMinutes * 60) }
    val totalTimeInSeconds = remember { initialTimeInMinutes * 60 }

    // Hitung progress (1.0 = penuh, 0.0 = habis)
    val progress by animateFloatAsState(
        targetValue = timeLeftInSeconds.toFloat() / totalTimeInSeconds.toFloat(),
        animationSpec = tween(durationMillis = 300),
        label = "progress"
    )

    // LaunchedEffect untuk countdown
    LaunchedEffect(timerState, timeLeftInSeconds) {
        if (timerState == TimerState.RUNNING && timeLeftInSeconds > 0) {
            delay(1000L)
            timeLeftInSeconds--
        } else if (timeLeftInSeconds == 0) {
            timerState = TimerState.IDLE
            // Timer selesai - bisa tambahkan notifikasi atau sound di sini
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pomodoro Timer") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = taskTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Circular Progress Indicator dengan Timer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(280.dp)
                    .padding(32.dp)
            ) {
                // Background circle
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 12.dp,
                )
                
                // Progress circle
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = when (timerState) {
                        TimerState.RUNNING -> MaterialTheme.colorScheme.primary
                        TimerState.PAUSED -> MaterialTheme.colorScheme.tertiary
                        TimerState.IDLE -> MaterialTheme.colorScheme.secondary
                    },
                    strokeWidth = 12.dp,
                )

                // Timer text di tengah
                Text(
                    text = formatTime(timeLeftInSeconds),
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Control Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                // Start/Pause Button
                FilledTonalButton(
                    onClick = {
                        timerState = when (timerState) {
                            TimerState.IDLE, TimerState.PAUSED -> TimerState.RUNNING
                            TimerState.RUNNING -> TimerState.PAUSED
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    if (timerState != TimerState.RUNNING) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Start",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = when (timerState) {
                            TimerState.RUNNING -> "⏸ Pause"
                            else -> "Start"
                        },
                        fontSize = 18.sp
                    )
                }

                // Stop/Reset Button
                OutlinedButton(
                    onClick = {
                        timerState = TimerState.IDLE
                        timeLeftInSeconds = totalTimeInSeconds
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    enabled = timerState != TimerState.IDLE
                ) {
                    Text(
                        text = "🔄 Reset",
                        fontSize = 18.sp
                    )
                }
            }
        }
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
