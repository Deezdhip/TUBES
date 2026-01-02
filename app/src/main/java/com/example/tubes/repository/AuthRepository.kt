package com.example.tubes.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

/**
 * Repository untuk mengelola Authentication operations.
 * Menggunakan Base64 encoding untuk profile image (tanpa Firebase Storage).
 */
class AuthRepository {
    
    companion object {
        private const val TAG = "AuthRepository"
        private const val MAX_IMAGE_SIZE = 500 // Max width/height in pixels
        private const val JPEG_QUALITY = 60 // Compression quality (0-100)
    }
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val userRepository = UserRepository()

    /**
     * Get current user email.
     */
    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }
    
    /**
     * Get current user photo URL.
     * Returns null since we now store Base64 in Firestore instead of URL.
     * Use getUserPhotoBase64() to get the Base64 string.
     */
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
    
    // ==================== BASE64 IMAGE ENCODING ====================
    
    /**
     * Encode image URI to Base64 string.
     * 
     * Process:
     * 1. Load bitmap from URI
     * 2. Resize to max 500x500 pixels
     * 3. Compress to JPEG with 60% quality
     * 4. Encode to Base64 string
     * 
     * @param uri Image URI from gallery picker
     * @param context Android context for content resolver
     * @return Base64 encoded string or null if failed
     */
    private fun encodeImageToBase64(uri: Uri, context: Context): String? {
        return try {
            // 1. Get input stream from URI
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw Exception("Cannot open image stream")
            
            // 2. Decode to Bitmap
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (originalBitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from URI")
                return null
            }
            
            // 3. Calculate resize dimensions (maintain aspect ratio)
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scaleFactor = if (width > height) {
                MAX_IMAGE_SIZE.toFloat() / width
            } else {
                MAX_IMAGE_SIZE.toFloat() / height
            }
            
            // 4. Resize bitmap if larger than max size
            val resizedBitmap = if (scaleFactor < 1f) {
                val newWidth = (width * scaleFactor).toInt()
                val newHeight = (height * scaleFactor).toInt()
                Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            } else {
                originalBitmap
            }
            
            // 5. Compress to JPEG
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
            val byteArray = outputStream.toByteArray()
            
            // Clean up
            if (resizedBitmap != originalBitmap) {
                resizedBitmap.recycle()
            }
            originalBitmap.recycle()
            
            // 6. Encode to Base64
            val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)
            
            Log.d(TAG, "Image encoded to Base64. Size: ${base64String.length} chars")
            
            base64String
            
        } catch (e: Exception) {
            Log.e(TAG, "Error encoding image to Base64", e)
            null
        }
    }
    
    /**
     * Upload profile image as Base64 string to Firestore.
     * Replaces Firebase Storage upload.
     * 
     * @param uri Image URI from gallery picker
     * @param context Android context for content resolver
     * @return Base64 string if successful
     */
    suspend fun uploadProfileImage(uri: Uri, context: Context): String {
        val user = auth.currentUser ?: throw Exception("User not logged in")
        
        // 1. Encode image to Base64
        val base64String = encodeImageToBase64(uri, context)
            ?: throw Exception("Failed to encode image")
        
        // 2. Save Base64 string to Firestore user document
        firestore.collection("users")
            .document(user.uid)
            .update("photoUrl", base64String)
            .await()
        
        Log.d(TAG, "Profile image saved to Firestore as Base64")
        
        return base64String
    }
    
    /**
     * Get user's profile photo as Base64 string from Firestore.
     * 
     * @return Base64 string or null if not found
     */
    suspend fun getUserPhotoBase64(): String? {
        val user = auth.currentUser ?: return null
        
        return try {
            val document = firestore.collection("users")
                .document(user.uid)
                .get()
                .await()
            
            document.getString("photoUrl")
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user photo", e)
            null
        }
    }
}
