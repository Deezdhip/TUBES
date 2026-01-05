package com.example.tubes.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

// ==================== CLEAN DARK THEME COLORS ====================
private val NavyDeep = Color(0xFF0D1B3E)
private val GlassWhite = Color.White.copy(alpha = 0.08f)
private val AccentCyan = Color(0xFF00D4FF)
private val TextWhite = Color.White
private val TextMuted = Color.White.copy(alpha = 0.6f)
private val ErrorRed = Color(0xFFFF6B6B)

/**
 * RegisterScreen - Clean Dark Premium
 * 
 * Design: Solid NavyDeep background, glassmorphism inputs, capsule button
 */
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    val focusManager = LocalFocusManager.current
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthUiState.Success) {
            onRegisterSuccess()
        }
    }

    val isLoading = authState is AuthUiState.Loading
    val errorMessage = (authState as? AuthUiState.Error)?.message

    // ==================== SOLID BACKGROUND ====================
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NavyDeep)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            
            // ==================== HEADER ====================
            Text(
                text = "Create",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                lineHeight = 44.sp
            )
            Text(
                text = "Account",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = AccentCyan,
                lineHeight = 44.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Start your productivity journey today",
                fontSize = 14.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ==================== NAME INPUT ====================
            TextField(
                value = name,
                onValueChange = { 
                    name = it
                    if (errorMessage != null) viewModel.clearError()
                },
                placeholder = { Text("Full Name", color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Rounded.Person, null, tint = TextMuted)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = GlassWhite,
                    unfocusedContainerColor = GlassWhite,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    cursorColor = AccentCyan
                ),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ==================== EMAIL INPUT ====================
            TextField(
                value = email,
                onValueChange = { 
                    email = it
                    if (errorMessage != null) viewModel.clearError()
                },
                placeholder = { Text("Email", color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Rounded.Email, null, tint = TextMuted)
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
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = GlassWhite,
                    unfocusedContainerColor = GlassWhite,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    cursorColor = AccentCyan
                ),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ==================== PASSWORD INPUT ====================
            TextField(
                value = password,
                onValueChange = { 
                    password = it
                    if (errorMessage != null) viewModel.clearError()
                },
                placeholder = { Text("Password", color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Rounded.Lock, null, tint = TextMuted)
                },
                trailingIcon = {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(
                            text = if (passwordVisible) "Hide" else "Show",
                            color = AccentCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = GlassWhite,
                    unfocusedContainerColor = GlassWhite,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    cursorColor = AccentCyan
                ),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ==================== CONFIRM PASSWORD INPUT ====================
            TextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    if (errorMessage != null) viewModel.clearError()
                },
                placeholder = { Text("Confirm Password", color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Rounded.Lock, null, tint = TextMuted)
                },
                trailingIcon = {
                    TextButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Text(
                            text = if (confirmPasswordVisible) "Hide" else "Show",
                            color = AccentCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                        viewModel.register(name, email, password, confirmPassword)
                    }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = GlassWhite,
                    unfocusedContainerColor = GlassWhite,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    cursorColor = AccentCyan
                ),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            // ==================== ERROR MESSAGE ====================
            AnimatedVisibility(visible = errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = ErrorRed,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            // ==================== REGISTER BUTTON ====================
            Button(
                onClick = { viewModel.register(name, email, password, confirmPassword) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = NavyDeep
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = NavyDeep,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Create Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ==================== FOOTER ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 14.sp,
                    color = TextMuted
                )
                TextButton(
                    onClick = onNavigateToLogin,
                    enabled = !isLoading,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Sign In",
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
