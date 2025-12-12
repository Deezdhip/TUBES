package com.example.tubes.repository

import com.example.tubes.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repository untuk mengelola data User di Firestore.
 */
class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val auth = FirebaseAuth.getInstance()

    /**
     * Menyimpan data user ke Firestore setelah registrasi.
     * 
     * @param uid User ID dari Firebase Auth
     * @param name Nama lengkap user
     * @param email Email user
     */
    suspend fun createUserProfile(uid: String, name: String, email: String) {
        try {
            val user = User(
                uid = uid,
                name = name,
                email = email,
                createdAt = System.currentTimeMillis()
            )
            
            // Simpan dengan document ID = user UID
            usersCollection.document(uid).set(user).await()
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Mendapatkan data user dari Firestore.
     * 
     * @param uid User ID
     * @return User object atau null jika tidak ditemukan
     */
    suspend fun getUserProfile(uid: String): User? {
        return try {
            val document = usersCollection.document(uid).get().await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Mendapatkan current user profile.
     * 
     * @return User object dari user yang sedang login, atau null
     */
    suspend fun getCurrentUserProfile(): User? {
        val currentUserId = auth.currentUser?.uid ?: return null
        return getUserProfile(currentUserId)
    }

    /**
     * Update user profile.
     * 
     * @param uid User ID
     * @param updates Map of field to update
     */
    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>) {
        try {
            usersCollection.document(uid).update(updates).await()
        } catch (e: Exception) {
            throw e
        }
    }
    /**
     * Update user name specifically.
     */
    suspend fun updateUserName(uid: String, name: String) {
        updateUserProfile(uid, mapOf("name" to name))
    }
}
