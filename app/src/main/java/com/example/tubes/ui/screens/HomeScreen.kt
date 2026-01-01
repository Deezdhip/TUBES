package com.example.tubes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import kotlinx.coroutines.launch

/**
 * Modern Project Manager Style - HomeScreen
 * Deep Blue Theme dengan grid layout
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
    var searchQuery by remember { mutableStateOf("") }
    
    val authRepository = remember { AuthRepository() }
    val userName = authViewModel.getCurrentUserName() ?: "User"
    
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Get greeting based on time of day
    val greeting = getGreeting()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Modern Bottom Navigation with FAB cradle
            ModernBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onAddClick = { showDialog = true }
            )
        },
        containerColor = Background,
        floatingActionButton = { },
        floatingActionButtonPosition = FabPosition.Center
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
                                CircularProgressIndicator(color = NavyDeep)
                            }
                        }

                        is TaskUiState.Success -> {
                            val tasks = state.tasks
                            val visibleTasks = if (searchQuery.isBlank()) {
                                tasks
                            } else {
                                tasks.filter { it.title.contains(searchQuery, ignoreCase = true) }
                            }
                            
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Background),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // ==================== HEADER (FULL WIDTH) ====================
                                item(span = { GridItemSpan(2) }) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 8.dp)
                                    ) {
                                        // App bar with menu
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Menu icon - Navigate to RecycleBin
                                            IconButton(
                                                onClick = { onNavigateToRecycleBin() }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Menu,
                                                    contentDescription = "Recycle Bin",
                                                    tint = TextDark,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            
                                            Text(
                                                text = "Home",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextDark
                                            )
                                            
                                            // Profile avatar - Navigate to Profile tab
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(AccentBlue)
                                                    .clickable { selectedTab = 3 },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = userName.firstOrNull()?.toString()?.uppercase() ?: "U",
                                                    color = TextOnBlue,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(24.dp))
                                        
                                        // Greeting
                                        Text(
                                            text = "Hi ${userName}!",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextDark
                                        )
                                        Text(
                                            text = greeting,
                                            fontSize = 14.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                
                                // ==================== SEARCH BAR (FULL WIDTH) ====================
                                item(span = { GridItemSpan(2) }) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp)
                                            .shadow(
                                                elevation = 8.dp,
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
                                                    "Search", 
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
                                                focusedTextColor = TextDark,
                                                unfocusedTextColor = TextDark
                                            ),
                                            singleLine = true
                                        )
                                    }
                                }
                                
                                // ==================== WELCOME BANNER (FULL WIDTH) ====================
                                item(span = { GridItemSpan(2) }) {
                                    WelcomeBanner()
                                }
                                
                                // ==================== SECTION HEADER (FULL WIDTH) ====================
                                item(span = { GridItemSpan(2) }) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Ongoing Projects",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextDark
                                        )
                                        Text(
                                            text = "view all",
                                            fontSize = 14.sp,
                                            color = TextSecondary,
                                            modifier = Modifier.clickable { /* TODO */ }
                                        )
                                    }
                                }
                                
                                // ==================== TASK GRID ====================
                                if (visibleTasks.isEmpty()) {
                                    item(span = { GridItemSpan(2) }) {
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
                                            // Klik Card = Toggle status (update isCompleted + progress)
                                            onClick = { t -> 
                                                viewModel.toggleTaskStatus(t)
                                            },
                                            // Menu: Mark Complete/Incomplete = Toggle status
                                            onCheckClick = { t, _ ->
                                                viewModel.toggleTaskStatus(t)
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
                                
                                // Bottom spacing
                                item(span = { GridItemSpan(2) }) {
                                    Spacer(modifier = Modifier.height(80.dp))
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
                                            containerColor = NavyDeep
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
                    // Timer Screen - user must select a task first
                    TimerScreen(
                        taskTitle = "Focus Session",
                        onNavigateBack = { selectedTab = 0 }
                    )
                }
                3 -> {
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

/**
 * Welcome Banner with illustration
 */
@Composable
private fun WelcomeBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = NavyDeep
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Welcome!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Let's schedule your\nprojects",
                    fontSize = 14.sp,
                    color = TextOnBlue.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )
            }
            
            // Illustration placeholder - Person icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AccentBlue.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Illustration",
                    tint = TextOnBlue,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

/**
 * Modern Bottom Navigation Bar with FAB in center
 */
@Composable
private fun ModernBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onAddClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // White background bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceWhite
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                BottomNavItem(
                    icon = Icons.Outlined.Home,
                    label = "Home",
                    isSelected = selectedTab == 0,
                    onClick = { onTabSelected(0) }
                )
                
                // Stats (Dashboard)
                BottomNavItem(
                    icon = Icons.Outlined.BarChart,
                    label = "Stats",
                    isSelected = selectedTab == 1,
                    onClick = { onTabSelected(1) }
                )
                
                // Spacer for FAB
                Spacer(modifier = Modifier.width(56.dp))
                
                // Timer
                BottomNavItem(
                    icon = Icons.Outlined.Timer,
                    label = "Timer",
                    isSelected = selectedTab == 2,
                    onClick = { onTabSelected(2) }
                )
                
                // Profile
                BottomNavItem(
                    icon = Icons.Outlined.Person,
                    label = "Profile",
                    isSelected = selectedTab == 3,
                    onClick = { onTabSelected(3) }
                )
            }
        }
        
        // FAB - Centered and elevated
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-24).dp)
                .size(56.dp),
            shape = CircleShape,
            containerColor = NavyDeep,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Task",
                tint = TextOnBlue,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/**
 * Bottom Navigation Item
 */
@Composable
private fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) AccentBlue else TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) AccentBlue else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

/**
 * Get greeting based on time of day
 */
private fun getGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good Morning"
        hour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }
}
