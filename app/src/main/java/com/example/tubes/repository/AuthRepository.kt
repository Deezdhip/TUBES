package com.example.tubes.repository

import com.google.firebase.auth.FirebaseAuth

/**
 * Repository untuk mengelola Authentication operations.
 */
class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    /**
     * Get current user email.
     */
    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
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
}
