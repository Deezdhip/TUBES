package com.example.tubes.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.tubes.ui.theme.*
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tubes.viewmodel.AuthUiState
import com.example.tubes.viewmodel.AuthViewModel


/**
 * LoginScreen - Modern, minimalis, dan menenangkan dengan MVVM pattern
 */
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val focusManager = LocalFocusManager.current
    val authState by viewModel.authState.collectAsState()

    // Handle success state
    LaunchedEffect(authState) {
        if (authState is AuthUiState.Success) {
            onLoginSuccess()
        }
    }

    val isLoading = authState is AuthUiState.Loading
    val errorMessage = (authState as? AuthUiState.Error)?.message

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Background Decoration
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Top Right Circle
            drawCircle(
                color = PrimaryBlue.copy(alpha = 0.15f),
                radius = 200.dp.toPx(),
                center = center.copy(x = size.width, y = 0f)
            )
            // Bottom Left Circle
            drawCircle(
                color = PrimaryBlue.copy(alpha = 0.1f),
                radius = 150.dp.toPx(),
                center = center.copy(x = 0f, y = size.height)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Spacer to push content a bit down if needed, or centering handles it
            
            // App Branding
            Text(
                text = "FocusTask",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Welcome Back!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Login Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            if (errorMessage != null) viewModel.clearError()
                        },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email Icon",
                                tint = PrimaryBlue
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BackgroundDark,
                            focusedContainerColor = BackgroundDark,
                            unfocusedContainerColor = BackgroundDark,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = OnSurfaceVariant
                        ),
                        enabled = !isLoading
                    )

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            if (errorMessage != null) viewModel.clearError()
                        },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password Icon",
                                tint = PrimaryBlue
                            )
                        },
                        trailingIcon = {
                            TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                Text(
                                    text = if (passwordVisible) "Hide" else "Show",
                                    color = PrimaryBlue,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { 
                                focusManager.clearFocus()
                                viewModel.login(email, password)
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BackgroundDark,
                            focusedContainerColor = BackgroundDark,
                            unfocusedContainerColor = BackgroundDark,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = OnSurfaceVariant
                        ),
                        enabled = !isLoading
                    )

                    // Login Button
                    Button(
                        onClick = { viewModel.login(email, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = Color.White
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Login",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            // Error Message (Outside card or close to it)
            AnimatedVisibility(visible = errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = WarningOrange,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Register Link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Belum punya akun? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
                TextButton(
                    onClick = onNavigateToRegister,
                    enabled = !isLoading
                ) {
                    Text(
                        text = "Daftar di sini",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
