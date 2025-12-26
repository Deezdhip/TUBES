package com.example.tubes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Delete
import kotlinx.coroutines.launch

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

/**
 * Modern Clean Minimalist HomeScreen
 * White background dengan Royal Blue accent
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTimer: (String) -> Unit,
    onNavigateToRecycleBin: () -> Unit,
    onLogout: () -> Unit,
    viewModel: TaskViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val authRepository = remember { AuthRepository() }
    val userName = authViewModel.getCurrentUserName() ?: "User"
    
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val context = LocalContext.current
    
    // Image Picker
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
            // Clean minimal top bar
            TopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            Icons.Default.List, 
                            contentDescription = "More",
                            tint = TextSecondary
                        )
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundLight,
                    actionIconContentColor = TextSecondary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Floating Bottom Navigation - Clean White
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                shape = RoundedCornerShape(50.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceWhite
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
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

                    // Add Task Action - Royal Blue accent
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
        containerColor = BackgroundLight
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (selectedTab) {
                0 -> {
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
                            val visibleTasks = if (searchQuery.isBlank()) {
                                tasks
                            } else {
                                tasks.filter { it.title.contains(searchQuery, ignoreCase = true) }
                            }
                            
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(BackgroundLight)
                            ) {
                                // ==================== CLEAN HEADER ====================
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(BackgroundLight)
                                            .padding(horizontal = 24.dp, vertical = 16.dp)
                                    ) {
                                        // Greeting - Large Bold Text
                                        Text(
                                            text = "Hi, ${userName}!",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        // Subtitle - Grey
                                        Text(
                                            text = "You have $inProgressTasks tasks pending",
                                            fontSize = 14.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                // ==================== FLOATING SEARCH BAR ====================
                                item {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 24.dp, vertical = 8.dp)
                                            .shadow(
                                                elevation = 4.dp,
                                                shape = RoundedCornerShape(50.dp),
                                                ambientColor = Color.Black.copy(alpha = 0.1f)
                                            ),
                                        shape = RoundedCornerShape(50.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = SurfaceWhite
                                        )
                                    ) {
                                        OutlinedTextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            placeholder = { 
                                                Text(
                                                    "Search tasks...", 
                                                    color = TextSecondary
                                                ) 
                                            },
                                            leadingIcon = { 
                                                Icon(
                                                    Icons.Default.Search, 
                                                    "Search", 
                                                    tint = TextSecondary
                                                ) 
                                            },
                                            shape = RoundedCornerShape(50.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = SurfaceWhite,
                                                unfocusedContainerColor = SurfaceWhite,
                                                focusedBorderColor = Color.Transparent,
                                                unfocusedBorderColor = Color.Transparent,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            ),
                                            singleLine = true
                                        )
                                    }
                                }

                                // ==================== SECTION HEADER ====================
                                item {
                                    Text(
                                        text = "Ongoing Tasks",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(
                                            horizontal = 24.dp, 
                                            vertical = 16.dp
                                        )
                                    )
                                }

                                // ==================== TASK LIST ====================
                                if (visibleTasks.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 48.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "🎉",
                                                    fontSize = 48.sp
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = if (searchQuery.isBlank()) 
                                                        "No tasks yet. Add one!" 
                                                    else 
                                                        "No tasks found",
                                                    color = TextSecondary,
                                                    fontSize = 16.sp
                                                )
                                            }
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
                                                viewModel.updateTaskStatus(t.id, isChecked) 
                                            },
                                            onDeleteClick = { t ->
                                                viewModel.deleteTask(t.id) 
                                                scope.launch {
                                                    val result = snackbarHostState.showSnackbar(
                                                        message = "Task moved to trash",
                                                        actionLabel = "UNDO",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                    if (result == SnackbarResult.ActionPerformed) {
                                                        viewModel.restoreTask(t)
                                                    }
                                                }
                                            },
                                            onPinClick = { t ->
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
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Oops! Something went wrong",
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { viewModel.clearError() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PrimaryBlue
                                        )
                                    ) {
                                        Text("Retry")
                                    }
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
            onAdd = { title, priority, category, dueDate ->
                viewModel.addTask(title, priority, category, dueDate)
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
            tint = if (isSelected) PrimaryBlue else TextSecondary,
            modifier = Modifier.size(28.dp)
        )
    }
}
