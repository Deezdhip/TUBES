package com.example.tubes.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tubes.ui.theme.*
import com.example.tubes.viewmodel.AuthUiState
import com.example.tubes.viewmodel.AuthViewModel
import com.example.tubes.viewmodel.TaskViewModel
import com.example.tubes.viewmodel.TaskUiState

/**
 * Modern Project Manager Style - ProfileScreen
 * Deep Blue curved header dengan stats dan categories
 */
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: AuthViewModel = viewModel(),
    taskViewModel: TaskViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val taskState by taskViewModel.uiState.collectAsState()
    
    val userName = viewModel.getCurrentUserName() ?: "User"
    val userEmail = viewModel.getCurrentUserEmail() ?: "email@example.com"
    val userPhotoUrl = viewModel.getCurrentUserPhotoUrl()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    
    // Calculate task statistics
    val (totalTasks, completedTasks, pendingTasks) = remember(taskState) {
        when (taskState) {
            is TaskUiState.Success -> {
                val tasks = (taskState as TaskUiState.Success).tasks
                Triple(
                    tasks.size,
                    tasks.count { it.isCompleted },
                    tasks.count { !it.isCompleted }
                )
            }
            else -> Triple(0, 0, 0)
        }
    }
    
    // Image Picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateProfile(name = userName, photoUri = uri)
        }
    }
    
    if (showEditNameDialog) {
        EditNameDialog(
            currentName = userName,
            onDismiss = { showEditNameDialog = false },
            onConfirm = { newName ->
                viewModel.updateProfile(name = newName, photoUri = null)
                showEditNameDialog = false
            }
        )
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = { newPassword ->
                viewModel.updatePassword(password = newPassword)
                showChangePasswordDialog = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Loading indicator
        if (authState is AuthUiState.Loading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = AccentBlue,
                trackColor = DividerGrey
            )
        }
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ==================== CURVED NAVY HEADER (40%) ====================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.42f)
                    .background(
                        color = NavyDeep,
                        shape = RoundedCornerShape(
                            bottomStart = 32.dp,
                            bottomEnd = 32.dp
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Profile Picture
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.size(100.dp)
                    ) {
                        if (userPhotoUrl != null) {
                            AsyncImage(
                                model = userPhotoUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, TextOnBlue.copy(alpha = 0.3f), CircleShape)
                                    .clickable { launcher.launch("image/*") },
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Placeholder with initial
                            Surface(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clickable { launcher.launch("image/*") },
                                shape = CircleShape,
                                color = AccentBlue.copy(alpha = 0.3f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = userName.firstOrNull()?.toString()?.uppercase() ?: "U",
                                        fontSize = 40.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextOnBlue
                                    )
                                }
                            }
                        }
                        
                        // Camera button
                        Surface(
                            shape = CircleShape,
                            color = AccentBlue,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { launcher.launch("image/*") },
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CameraAlt, 
                                    "Change Photo", 
                                    modifier = Modifier.size(16.dp),
                                    tint = TextOnBlue
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Name (White, Large)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.clickable { showEditNameDialog = true }
                    ) {
                        Text(
                            text = userName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextOnBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Edit, 
                            "Edit Name",
                            tint = TextOnBlue.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    // Role/Email (White, Small)
                    Text(
                        text = "UI/UX Designer", // Could be dynamic
                        fontSize = 14.sp,
                        color = TextOnBlue.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // ==================== STATS ROW ====================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            value = totalTasks.toString(),
                            label = "Total Tasks"
                        )
                        StatItem(
                            value = completedTasks.toString(),
                            label = "Completed"
                        )
                        StatItem(
                            value = pendingTasks.toString(),
                            label = "Pending"
                        )
                    }
                }
            }
            
            // ==================== BODY CONTENT ====================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Categories Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categories",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Text(
                        text = "view all",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Categories Grid (2x2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CategoryCard(
                        icon = Icons.Default.Work,
                        name = "Work",
                        color = CategoryWork,
                        modifier = Modifier.weight(1f)
                    )
                    CategoryCard(
                        icon = Icons.Default.Person,
                        name = "Personal",
                        color = CategoryPersonal,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CategoryCard(
                        icon = Icons.Default.School,
                        name = "Study",
                        color = CategoryStudy,
                        modifier = Modifier.weight(1f)
                    )
                    CategoryCard(
                        icon = Icons.Default.MoreHoriz,
                        name = "Others",
                        color = CategoryOthers,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // ==================== ACTION BUTTONS ====================
                Text(
                    text = "Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Change Password Button
                OutlinedButton(
                    onClick = { showChangePasswordDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NavyDeep
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyDeep)
                ) {
                    Icon(
                        Icons.Default.Lock, 
                        null, 
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Change Password", 
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                // Logout Button
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed
                    )
                ) {
                    Icon(
                        Icons.Default.Logout, 
                        null, 
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Logout", 
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Stats item for profile header
 */
@Composable
private fun StatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextOnBlue
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextOnBlue.copy(alpha = 0.7f)
        )
    }
}

/**
 * Category Card for categories grid
 */
@Composable
private fun CategoryCard(
    icon: ImageVector,
    name: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark
            )
        }
    }
}

// ==================== DIALOGS ====================

@Composable
fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Edit Display Name", 
                color = TextDark,
                fontWeight = FontWeight.SemiBold
            ) 
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextDark,
                    unfocusedTextColor = TextDark,
                    focusedBorderColor = NavyDeep,
                    unfocusedBorderColor = DividerGrey,
                    focusedLabelColor = NavyDeep,
                    unfocusedLabelColor = TextSecondary
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) {
                Text("Save", color = NavyDeep, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Change Password", 
                color = TextDark,
                fontWeight = FontWeight.SemiBold
            ) 
        },
        text = {
            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("New Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedBorderColor = NavyDeep,
                        unfocusedBorderColor = DividerGrey,
                        focusedLabelColor = NavyDeep,
                        unfocusedLabelColor = TextSecondary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedBorderColor = if (errorMessage != null) ErrorRed else NavyDeep,
                        unfocusedBorderColor = if (errorMessage != null) ErrorRed else DividerGrey,
                        focusedLabelColor = NavyDeep,
                        unfocusedLabelColor = TextSecondary
                    ),
                    isError = errorMessage != null
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (password.length < 6) {
                    errorMessage = "Password must be at least 6 characters"
                } else if (password != confirmPassword) {
                    errorMessage = "Passwords do not match"
                } else {
                    onConfirm(password)
                }
            }) {
                Text("Update", color = NavyDeep, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(24.dp)
    )
}
