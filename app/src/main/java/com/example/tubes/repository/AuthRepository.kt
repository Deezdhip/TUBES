package com.example.tubes.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Repository untuk mengelola Authentication operations.
 */
class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val userRepository = UserRepository()

    /**
     * Get current user email.
     */
    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }
    
    fun getCurrentUserPhotoUrl(): Uri? {
        return auth.currentUser?.photoUrl
    }

    /**
     * Get current user display name.
     */
    fun getCurrentUserName(): String? {
        return auth.currentUser?.displayName
    }

    /**
     * Check if user is logged in.
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Logout current user.
     */
    fun logout() {
        auth.signOut()
    }
    
    /**
     * Update user display name
     */
    suspend fun updateDisplayName(name: String) {
        val user = auth.currentUser ?: throw Exception("User not logged in")
        
        user.updateProfile(userProfileChangeRequest {
            displayName = name
        }).await()
        
        // Also update in Firestore if needed
        userRepository.updateUserName(user.uid, name)
    }
    
    /**
     * Update user password
     */
    suspend fun updatePassword(password: String) {
        val user = auth.currentUser ?: throw Exception("User not logged in")
        user.updatePassword(password).await()
    }
    
    /**
     * Upload profile image and return download URL
     */
    suspend fun uploadProfileImage(uri: Uri): Uri {
        val user = auth.currentUser ?: throw Exception("User not logged in")
        val ref = storage.reference.child("profile_images/${user.uid}.jpg")
        
        ref.putFile(uri).await()
        val downloadUrl = ref.downloadUrl.await()
        
        // Update auth profile
        user.updateProfile(userProfileChangeRequest {
            photoUri = downloadUrl
        }).await()
        
        return downloadUrl
    }
}
