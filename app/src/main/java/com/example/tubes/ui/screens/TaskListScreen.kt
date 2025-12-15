package com.example.tubes.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tubes.ui.components.TaskItem
import com.example.tubes.ui.theme.*
import com.example.tubes.viewmodel.TaskUiState
import com.example.tubes.viewmodel.TaskViewModel
import com.example.tubes.ui.components.AddTaskDialog

/**
 * Screen utama untuk menampilkan daftar task.
 * Menggunakan LazyColumn dan FAB untuk menambah task baru.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel = viewModel(),
    onNavigateToTimer: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Tugas", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = OnBackgroundWhite
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Tugas"
                )
            }
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
                    radius = 200.dp.toPx(),
                    center = center.copy(x = size.width, y = 0f)
                )
                drawCircle(
                    color = PrimaryBlue.copy(alpha = 0.1f),
                    radius = 150.dp.toPx(),
                    center = center.copy(x = 0f, y = size.height)
                )
            }

            when (val state = uiState) {
                is TaskUiState.Loading -> {
                    // Loading indicator di tengah
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryBlue
                    )
                }

                is TaskUiState.Success -> {
                    if (state.tasks.isEmpty()) {
                        // Empty state
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Belum ada tugas",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = OnBackgroundWhite
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tambahkan tugas baru dengan tombol + di bawah.",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge,
                                color = OnSurfaceVariant
                            )
                        }
                    } else {
                        // Daftar tasks
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = state.tasks,
                                key = { task -> task.id }
                            ) { task ->
                                TaskItem(
                                    task = task,
                                    onClick = { onNavigateToTimer(it.title) },
                                    onCheckClick = { t, isCompleted ->
                                        viewModel.updateTaskStatus(t.id, isCompleted)
                                    },
                                    onDeleteClick = { t ->
                                        viewModel.deleteTask(t.id)
                                    },
                                    onPinClick = { t ->
                                        viewModel.togglePin(t)
                                    }
                                )
                            }
                        }
                    }
                }

                is TaskUiState.Error -> {
                    // Error state
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = WarningOrange,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.clearError() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }
            }
        }
    }

    // Dialog untuk menambah task baru
    if (showDialog) {
        AddTaskDialog(
            onDismiss = { showDialog = false },
            onAdd = { title, priority, category ->
                viewModel.addTask(title, priority, category, null)
                showDialog = false
            }
        )
    }
}
