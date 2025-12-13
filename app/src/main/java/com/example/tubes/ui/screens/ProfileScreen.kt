package com.example.tubes.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tubes.ui.theme.*
import com.example.tubes.viewmodel.AuthUiState
import com.example.tubes.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val userName = viewModel.getCurrentUserName() ?: "User"
    val userEmail = viewModel.getCurrentUserEmail() ?: "Email"
    val userPhotoUrl = viewModel.getCurrentUserPhotoUrl()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    
    // Image Picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateProfile(name = userName, photoUri = uri)
        }
    }

    // Success/Error Handling (Simple Toast-like effect or logging for now)
    // Ideally use SnackbarHostState, but for minimal error we'll rely on UI state updates visually
    
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
            .background(BackgroundDark)
    ) {
        // Background Decoration
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = PrimaryBlue.copy(alpha = 0.15f),
                radius = 300.dp.toPx(),
                center = center.copy(x = size.width, y = 0f)
            )
        }

        if (authState is AuthUiState.Loading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryBlue,
                trackColor = SurfaceCard
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Profile
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, 
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)
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
                                .clickable { launcher.launch("image/*") },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier
                                .size(120.dp)
                                .clickable { launcher.launch("image/*") },
                            shape = CircleShape,
                            color = PrimaryBlue
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = userName.firstOrNull()?.toString()?.uppercase() ?: "U",
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    
                    // Edit Photo Icon
                    Surface(
                        shape = CircleShape,
                        color = SurfaceCard,
                        modifier = Modifier
                            .size(36.dp)
                            .padding(2.dp)
                            .clickable { launcher.launch("image/*") },
                        border = androidx.compose.foundation.BorderStroke(2.dp, BackgroundDark)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Edit, 
                                "Edit Photo", 
                                modifier = Modifier.size(18.dp),
                                tint = PrimaryBlue
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Name Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.clickable { showEditNameDialog = true }
                ) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.Edit, 
                        "Edit Name",
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            // Actions Section
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Change Password Button
                Button(
                    onClick = { showChangePasswordDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceCard, 
                        contentColor = PrimaryBlue
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Lock, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Change Password", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                // Logout Button
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarningOrange.copy(alpha = 0.15f), 
                        contentColor = WarningOrange
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningOrange.copy(alpha = 0.3f))
                ) {
                    Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Display Name", color = OnBackgroundWhite) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display Name") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = OnBackgroundWhite,
                    unfocusedTextColor = OnBackgroundWhite,
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = OnSurfaceVariant
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) {
                Text("Save", color = PrimaryBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = OnSurfaceVariant)
            }
        },
        containerColor = SurfaceCard,
        titleContentColor = OnBackgroundWhite,
        textContentColor = OnBackgroundWhite
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
        title = { Text("Change Password", color = OnBackgroundWhite) },
        text = {
            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("New Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnBackgroundWhite,
                        unfocusedTextColor = OnBackgroundWhite,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = OnSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnBackgroundWhite,
                        unfocusedTextColor = OnBackgroundWhite,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = OnSurfaceVariant
                    ),
                    isError = errorMessage != null
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = WarningOrange,
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
                Text("Update", color = PrimaryBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = OnSurfaceVariant)
            }
        },
        containerColor = SurfaceCard,
        titleContentColor = OnBackgroundWhite,
        textContentColor = OnBackgroundWhite
    )
}
