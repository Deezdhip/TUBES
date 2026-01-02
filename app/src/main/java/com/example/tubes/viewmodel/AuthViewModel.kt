package com.example.tubes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubes.repository.AuthRepository
import com.example.tubes.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * AuthViewModel - Manages authentication state and operations
 */
class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    /**
     * Login user with email and password
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            // Validation
            if (email.isBlank() || password.isBlank()) {
                _authState.value = AuthUiState.Error("Email dan password tidak boleh kosong")
                return@launch
            }

            _authState.value = AuthUiState.Loading

            try {
                // Firebase Auth - Sign In
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthUiState.Success
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("no user record", ignoreCase = true) == true ->
                        "Email tidak terdaftar"
                    e.message?.contains("password is invalid", ignoreCase = true) == true ->
                        "Password salah"
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "Tidak ada koneksi internet"
                    else -> "Login gagal: ${e.message}"
                }
                _authState.value = AuthUiState.Error(errorMessage)
            }
        }
    }

    /**
     * Register new user
     */
    fun register(name: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            // Validation
            when {
                name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                    _authState.value = AuthUiState.Error("Semua field harus diisi")
                    return@launch
                }
                password != confirmPassword -> {
                    _authState.value = AuthUiState.Error("Password tidak cocok")
                    return@launch
                }
                password.length < 6 -> {
                    _authState.value = AuthUiState.Error("Password minimal 6 karakter")
                    return@launch
                }
            }

            _authState.value = AuthUiState.Loading

            try {
                // Step 1: Firebase Auth - Create User
                val result = auth.createUserWithEmailAndPassword(email, password).await()

                val userId = result.user?.uid
                if (userId != null) {
                    // Step 2: Update profile with display name
                    result.user?.updateProfile(
                        userProfileChangeRequest {
                            displayName = name
                        }
                    )?.await()

                    // Step 3: Save user data to Firestore
                    userRepository.createUserProfile(
                        uid = userId,
                        name = name,
                        email = email
                    )

                    _authState.value = AuthUiState.Success
                } else {
                    _authState.value = AuthUiState.Error("Gagal membuat akun")
                }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("email address is already in use", ignoreCase = true) == true ->
                        "Email sudah terdaftar"
                    e.message?.contains("email address is badly formatted", ignoreCase = true) == true ->
                        "Format email tidak valid"
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "Tidak ada koneksi internet"
                    else -> "Registrasi gagal: ${e.message}"
                }
                _authState.value = AuthUiState.Error(errorMessage)
            }
        }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _authState.value = AuthUiState.Idle
    }

    /**
     * Update user profile (name and/or photo)
     * Uses Base64 encoding for photo (no Firebase Storage)
     */
    fun updateProfile(name: String, photoUri: android.net.Uri?, context: android.content.Context? = null) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            try {
                // Update photo if provided (requires Context for Base64 encoding)
                if (photoUri != null && context != null) {
                    authRepository.uploadProfileImage(photoUri, context)
                }
                
                // Update name if provided and different
                if (name.isNotBlank() && name != authRepository.getCurrentUserName()) {
                    authRepository.updateDisplayName(name)
                }
                
                _authState.value = AuthUiState.Success
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error("Gagal update profil: ${e.message}")
            }
        }
    }

    /**
     * Update password
     */
    fun updatePassword(password: String) {
        viewModelScope.launch {
            if (password.length < 6) {
                _authState.value = AuthUiState.Error("Password minimal 6 karakter")
                return@launch
            }
            
            _authState.value = AuthUiState.Loading
            try {
                authRepository.updatePassword(password)
                _authState.value = AuthUiState.Success
            } catch (e: Exception) {
                val msg = if (e.message?.contains("requires recent login") == true) {
                    "Silakan login ulang untuk mengganti password"
                } else {
                    "Gagal ganti password: ${e.message}"
                }
                _authState.value = AuthUiState.Error(msg)
            }
        }
    }

    fun getCurrentUserPhotoUrl(): android.net.Uri? {
        return authRepository.getCurrentUserPhotoUrl()
    }
    
    fun getCurrentUserName(): String? {
        return authRepository.getCurrentUserName()
    }
    
    fun getCurrentUserEmail(): String? {
        return authRepository.getCurrentUserEmail()
    }

    /**
     * Check if user is already logged in
     */
    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }
}

/**
 * UI State for Authentication
 */
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
