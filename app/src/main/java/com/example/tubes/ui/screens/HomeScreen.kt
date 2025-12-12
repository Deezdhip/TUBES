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
import androidx.compose.material.icons.filled.Settings
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
    onLogout: () -> Unit,
    viewModel: TaskViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    
    var showDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    
    val authRepository = remember { AuthRepository() }
    val userEmail = remember { authRepository.getCurrentUserEmail() ?: "User" }
    val userName = authViewModel.getCurrentUserName() ?: "User"
    val userPhotoUrl = authViewModel.getCurrentUserPhotoUrl()
    
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

    Scaffold(
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
                NavigationBar(
                    containerColor = Color.Transparent,
                    modifier = Modifier.height(70.dp)
                ) {
                    // Home Tab
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, "Home") },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            unselectedIconColor = OnSurfaceVariant,
                            indicatorColor = Color.Transparent
                        )
                    )

                    // Add Task Action
                    NavigationBarItem(
                        icon = { 
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(PrimaryBlue, shape = RoundedCornerShape(50))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add, 
                                    "Add Task",
                                    tint = Color.White
                                )
                            }
                        },
                        selected = false,
                        onClick = { showDialog = true },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )

                    // Profile Tab
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, "Profile") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            unselectedIconColor = OnSurfaceVariant,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        // Content Switching based on Tab
        if (selectedTab == 0) {
            // Home Dashboard
            when (val state = uiState) {
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
                    val doneTasks = tasks.count { it.isCompleted }
                    val inProgressTasks = tasks.count { !it.isCompleted }
                    val ongoingTasks = tasks.filter { !it.isCompleted }
                    
                    LazyColumn(
                        modifier = modifier
                            .fillMaxSize()
                            .background(BackgroundDark)
                            .padding(paddingValues)
                    ) {
                    // Gradient Header with Decorative Circles
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
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
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.1f),
                                    radius = 60.dp.toPx(),
                                    center = center.copy(x = 50.dp.toPx(), y = size.height - 30.dp.toPx())
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

                    // Search Bar
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

                    // Statistics Grid (2x2)
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                StatCard(
                                    title = "Done",
                                    count = doneTasks,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    title = "In Progress",
                                    count = inProgressTasks,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                StatCard(
                                    title = "Ongoing",
                                    count = ongoingTasks.size,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    title = "Waiting for Review",
                                    count = 0,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Ongoing Tasks Section
                    item {
                        Text(
                            text = "Ongoing",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBackgroundWhite,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                        )
                    }

                    // Task List
                    if (ongoingTasks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No ongoing tasks",
                                    color = OnSurfaceVariant,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    } else {
                        items(ongoingTasks) { task ->
                            TaskItem(
                                task = task,
                                onClick = { onNavigateToTimer(task.title) },
                                onCheckedChange = { viewModel.updateTaskStatus(task.id, it) },
                                onDelete = { viewModel.deleteTask(task.id) }
                            )
                        }
                    }

                    // Bottom spacing
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            color = WarningOrange,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.clearError() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
        } else {
             // Profile Screen Content
             Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark)
                    .padding(paddingValues)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Profile Picture
                Box(contentAlignment = Alignment.BottomEnd) {
                    if (userPhotoUrl != null) {
                        AsyncImage(
                            model = userPhotoUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .border(2.dp, PrimaryBlue, CircleShape)
                                .clickable { launcher.launch("image/*") },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(120.dp)
                                .background(SurfaceCard, CircleShape)
                                .padding(24.dp)
                                .clickable { launcher.launch("image/*") },
                            tint = PrimaryBlue
                        )
                    }
                    
                    // Edit Icon Overlay
                    Box(
                        modifier = Modifier
                            .offset(x = 4.dp, y = 4.dp)
                            .background(PrimaryBlue, CircleShape)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Change Photo",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Name Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showEditNameDialog = true }
                ) {
                    Text(
                        text = userName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = com.example.tubes.ui.theme.OnBackgroundWhite
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Name",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Text(
                    text = userEmail,
                    fontSize = 14.sp,
                    color = OnSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Change Password Button
                OutlinedButton(
                    onClick = { showChangePasswordDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ganti Password")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Logout Button
                Button(
                    onClick = {
                        authRepository.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningOrange),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Logout", fontWeight = FontWeight.Bold)
                }
                
                // Loading State
                if (authState is AuthUiState.Loading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(color = PrimaryBlue)
                }
                
                // Error Message
                if (authState is AuthUiState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = (authState as AuthUiState.Error).message,
                        color = WarningOrange,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
    
    // Helper Dialogs
    if (showEditNameDialog) {
        var newName by remember { mutableStateOf(userName) }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Ubah Nama Panggilan") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nama") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    authViewModel.updateProfile(name = newName, photoUri = null)
                    showEditNameDialog = false
                }) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
    
    if (showChangePasswordDialog) {
        var newPassword by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Ganti Password") },
            text = {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Password Baru (min 6 karakter)") },
                    singleLine = true,
                    // visualTransformation = PasswordVisualTransformation() // Removed for simplicity/visibility, better to add if needed
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    authViewModel.updatePassword(password = newPassword)
                    showChangePasswordDialog = false
                }) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Add Task Dialog
    if (showDialog) {
        AddTaskDialog(
            onDismiss = { showDialog = false },
            onAdd = { title ->
                viewModel.addTask(title)
                showDialog = false
            }
        )
    }
}

/**
 * Statistics Card Component
 */
@Composable
fun StatCard(
    title: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = OnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = count.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
        }
    }
}


