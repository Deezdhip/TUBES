package com.example.tubes.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Delete
import kotlinx.coroutines.launch
// Imports removed/added directly in this block replacement is risky if not careful with lines. 
// I will target the Dashboard import line first.

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tubes.repository.AuthRepository
import com.example.tubes.ui.components.TaskItem
import com.example.tubes.ui.theme.*
import com.example.tubes.viewmodel.TaskUiState
import com.example.tubes.viewmodel.TaskViewModel
import com.example.tubes.ui.components.AddTaskDialog
import com.example.tubes.viewmodel.AuthViewModel
import com.example.tubes.viewmodel.AuthUiState
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Edit
import android.net.Uri
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

/**
 * Modern Dashboard HomeScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTimer: (String) -> Unit,
    onNavigateToRecycleBin: () -> Unit, // New parameter
    onLogout: () -> Unit,
    viewModel: TaskViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    
    var showDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) } // State for dropdown menu
    
    val authRepository = remember { AuthRepository() }
    val userEmail = remember { authRepository.getCurrentUserEmail() ?: "User" }
    val userName = authViewModel.getCurrentUserName() ?: "User"
    val userPhotoUrl = authViewModel.getCurrentUserPhotoUrl()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val context = LocalContext.current
    
    // Image Picker (preserved but not currently attached to UI in this version)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            authViewModel.updateProfile(name = userName, photoUri = uri)
        }
    }


    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
             CenterAlignedTopAppBar(
                 title = { Text("Task Manager", fontWeight = FontWeight.Bold) },
                 actions = {
                     IconButton(onClick = { showMenu = !showMenu }) {
                         Icon(Icons.Default.List, contentDescription = "More")
                     }
                     DropdownMenu(
                         expanded = showMenu,
                         onDismissRequest = { showMenu = false }
                     ) {
                         DropdownMenuItem(
                             text = { Text("Recycle Bin") },
                             onClick = {
                                 showMenu = false
                                 onNavigateToRecycleBin()
                             },
                             leadingIcon = {
                                 Icon(Icons.Default.Delete, contentDescription = null)
                             }
                         )
                     }
                 },
                 colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                     containerColor = BackgroundDark,
                     titleContentColor = Color.White,
                     actionIconContentColor = Color.White
                 )
             )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Floating Bottom Navigation
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                shape = RoundedCornerShape(50.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceCard
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(horizontal = 16.dp), // Increased padding for better spacing
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween // SpaceBetween works well for 4 items
                ) {
                    // Home Tab
                    NavigationIcon(
                        icon = Icons.Default.Home,
                        isSelected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )

                    // Dashboard Tab
                    NavigationIcon(
                        icon = Icons.Default.List,
                        isSelected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )

                    // Add Task Action (Inside Navigation)
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue)
                            .clickable { showDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Task",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Profile Tab
                    NavigationIcon(
                        icon = Icons.Default.Person,
                        isSelected = selectedTab == 2,
                        onClick = { selectedTab = 2 }
                    )
                }
            }
        },
        containerColor = BackgroundDark 
    ) { paddingValues ->
        // Content Switching based on Tab
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (selectedTab) {
                0 -> {
                    // Home Dashboard Logic
                    val state = uiState
                    when (state) {
                        is TaskUiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = PrimaryBlue)
                            }
                        }

                        is TaskUiState.Success -> {
                            val tasks = state.tasks
                            val inProgressTasks = tasks.count { !it.isCompleted }
                            val visibleTasks = tasks // Show all tasks (repo handles isDeleted)
                            
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(BackgroundDark)
                            ) {
                                // Gradient Header
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(240.dp) // Taller header to accommodate content
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        PrimaryBlue,
                                                        PrimaryBlue.copy(alpha = 0.8f)
                                                    )
                                                )
                                            )
                                    ) {
                                        // Decorative circles
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            drawCircle(
                                                color = Color.White.copy(alpha = 0.1f),
                                                radius = 100.dp.toPx(),
                                                center = center.copy(x = size.width - 50.dp.toPx(), y = 50.dp.toPx())
                                            )
                                        }

                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(24.dp),
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "Hi, ${userName}!",
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "You have ${inProgressTasks} tasks pending",
                                                fontSize = 16.sp,
                                                color = Color.White.copy(alpha = 0.9f)
                                            )
                                        }
                                    }
                                }

                                // Search Bar Overlap
                                item {
                                    OutlinedTextField(
                                        value = "",
                                        onValueChange = {},
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 24.dp)
                                            .offset(y = (-30).dp),
                                        placeholder = { Text("Search tasks...", color = OnSurfaceVariant) },
                                        leadingIcon = { 
                                            Icon(Icons.Default.Search, "Search", tint = OnSurfaceVariant) 
                                        },
                                        shape = RoundedCornerShape(50.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = SurfaceCard,
                                            unfocusedContainerColor = SurfaceCard,
                                            focusedBorderColor = PrimaryBlue,
                                            unfocusedBorderColor = Color.Transparent
                                        )
                                    )
                                }

                                // Ongoing Tasks Header
                                item {
                                    Text(
                                        text = "Ongoing Tasks",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnBackgroundWhite,
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                    )
                                }

                                // Task List
                                if (visibleTasks.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No tasks yet. Add one!",
                                                color = OnSurfaceVariant,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                } else {
                                    items(
                                        items = visibleTasks,
                                        key = { it.id }
                                    ) { task ->
                                        TaskItem(
                                            task = task,
                                            onClick = { onNavigateToTimer(it.title) },
                                            onCheckClick = { t, isChecked ->
                                                android.widget.Toast.makeText(context, "Updating status...", android.widget.Toast.LENGTH_SHORT).show()
                                                viewModel.updateTaskStatus(t.id, isChecked) 
                                            },
                                            onDeleteClick = { t ->
                                                viewModel.deleteTask(t.id) 
                                                // Show Undo Snackbar
                                                scope.launch {
                                                    val result = snackbarHostState.showSnackbar(
                                                        message = "Tugas dipindahkan ke sampah",
                                                        actionLabel = "BATAL",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                    if (result == SnackbarResult.ActionPerformed) {
                                                        viewModel.restoreTask(t)
                                                    }
                                                }
                                            },
                                            onPinClick = { t ->
                                                val msg = if (t.isPinned) "Unpinning..." else "Pinning..."
                                                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                                viewModel.togglePin(t) 
                                            }
                                        )
                                    }
                                }
                                
                                item {
                                    Spacer(modifier = Modifier.height(100.dp))
                                }
                            }
                        }
                        
                         is TaskUiState.Error -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(onClick = { viewModel.clearError() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
                1 -> {
                     DashboardScreen()
                }
                2 -> {
                     ProfileScreen(
                         onLogout = {
                             authRepository.logout()
                             onLogout()
                         }
                     )
                }
            }
        }
    }

    // Add Task Dialog
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

@Composable
private fun NavigationIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 24.dp)
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) PrimaryBlue else OnSurfaceVariant,
            modifier = Modifier.size(28.dp)
        )
    }
}
