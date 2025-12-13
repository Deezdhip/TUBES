package com.example.tubes.repository

import com.example.tubes.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository untuk mengelola operasi CRUD Task dengan Firestore.
 * Menggunakan Flow untuk real-time updates.
 * Support multi-user dengan Firebase Authentication.
 */
class TaskRepository {
    private val db = FirebaseFirestore.getInstance()
    private val tasksCollection = db.collection("tasks")
    private val auth = FirebaseAuth.getInstance()

    /**
     * Mendapatkan current user ID.
     * Null jika user belum login.
     */
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Mendapatkan semua tasks milik user yang sedang login secara real-time menggunakan Flow.
     * Data akan otomatis update ketika ada perubahan di Firestore.
     * 
     * @return Flow yang emit list tasks milik user, atau empty list jika user belum login
     */
    fun getTasks(): Flow<List<Task>> = callbackFlow {
        val currentUserId = getCurrentUserId()
        
        // Jika user belum login, emit empty list
        if (currentUserId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = tasksCollection
            .whereEqualTo("userId", currentUserId)  // Filter hanya task milik user ini
            // orderBy dihapus untuk menghindari composite index requirement
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val tasks = snapshot.documents.mapNotNull { document ->
                        document.toObject(Task::class.java)?.apply {
                            id = document.id
                        }
                    }
                    // Sort by timestamp descending di client-side
                    val sortedTasks = tasks.sortedByDescending { it.timestamp }
                    trySend(sortedTasks)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Menambahkan task baru ke Firestore dengan userId dari user yang sedang login.
     * 
     * @param title Judul task yang akan ditambahkan
     * @throws IllegalStateException jika user belum login
     */
    suspend fun addTask(title: String, priority: String, category: String) {
        try {
            val currentUserId = getCurrentUserId()
                ?: throw IllegalStateException("User belum login")

            val task = Task(
                userId = currentUserId,  // Set userId dari user yang login
                title = title,
                isCompleted = false,
                priority = priority,
                category = category,
                timestamp = System.currentTimeMillis()
            )
            tasksCollection.add(task).await()
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Mengupdate status penyelesaian task.
     * Hanya bisa update task milik user sendiri (security handled by Firestore rules).
     * 
     * @param taskId ID dokumen task di Firestore
     * @param isCompleted Status baru untuk task
     */
    suspend fun updateTaskStatus(taskId: String, isCompleted: Boolean) {
        try {
            tasksCollection.document(taskId)
                .update("isCompleted", isCompleted)
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Menghapus task dari Firestore.
     * Hanya bisa hapus task milik user sendiri (security handled by Firestore rules).
     * 
     * @param taskId ID dokumen task yang akan dihapus
     */
    suspend fun deleteTask(taskId: String) {
        try {
            tasksCollection.document(taskId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw e
        }
    }
}
