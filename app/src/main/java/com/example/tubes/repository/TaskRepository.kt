package com.example.tubes.repository

import com.example.tubes.model.Task
import com.example.tubes.util.DateUtils
import com.example.tubes.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/**
 * Repository untuk mengelola operasi CRUD Task dengan Firestore.
 * Menggunakan Kotlin Coroutines Flow untuk real-time updates.
 * Support multi-user dengan Firebase Authentication.
 */
class TaskRepository {
    
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val tasksCollection = db.collection("tasks")

    // ==================== HELPER FUNCTIONS ====================
    
    /**
     * Mendapatkan current user ID.
     * @return User ID atau null jika user belum login.
     */
    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Sorting comparator untuk tasks.
     * Urutan prioritas:
     * 1. isPinned (desc) - Pinned tasks di atas
     * 2. isOverdue (desc) - Overdue tasks (belum selesai) diprioritaskan
     * 3. isCompleted (asc) - Belum selesai di atas
     * 4. dueDate (asc, nulls last) - Deadline terdekat di atas
     * 5. timestamp (desc) - Terbaru di atas
     */
    private fun getTaskComparator(): Comparator<Task> = compareByDescending<Task> { it.isPinned }
        .thenByDescending { !it.isCompleted && DateUtils.isOverdue(it.dueDate) } // Overdue di atas
        .thenBy { it.isCompleted }
        .thenBy(nullsLast()) { it.dueDate }
        .thenByDescending { it.timestamp }

    /**
     * Mengonversi Firestore document ke Task object dengan ID.
     */
    private fun documentToTask(document: com.google.firebase.firestore.DocumentSnapshot): Task? {
        return document.toObject(Task::class.java)?.copy(id = document.id)
    }

    // ==================== READ OPERATIONS ====================

    /**
     * Mendapatkan semua tasks (non-deleted) milik user yang sedang login secara real-time.
     * Data akan otomatis update ketika ada perubahan di Firestore.
     * 
     * Sorting:
     * 1. isPinned (Descending) - Pinned tasks di atas
     * 2. isOverdue (Descending) - Overdue tasks diprioritaskan
     * 3. isCompleted (Ascending) - Incomplete tasks di atas
     * 4. dueDate (Ascending) - Deadline terdekat di atas, null di bawah
     * 5. timestamp (Descending) - Terbaru di atas
     * 
     * @return Flow<Resource<List<Task>>> dengan state Loading, Success, atau Error
     */
    fun getTasks(): Flow<Resource<List<Task>>> = callbackFlow {
        // Emit loading state
        trySend(Resource.Loading())
        
        val currentUserId = getCurrentUserId()
        
        // Jika user belum login, emit error
        if (currentUserId == null) {
            trySend(Resource.Error("User belum login"))
            close()
            return@callbackFlow
        }

        val listener = tasksCollection
            .whereEqualTo("userId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Terjadi kesalahan saat mengambil data"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    try {
                        val tasks = snapshot.documents
                            .mapNotNull { documentToTask(it) }
                            .filter { !it.isDeleted }
                            .sortedWith(getTaskComparator())
                        
                        trySend(Resource.Success(tasks))
                    } catch (e: Exception) {
                        trySend(Resource.Error(e.message ?: "Gagal memproses data"))
                    }
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Mendapatkan semua deleted tasks milik user untuk Recycle Bin.
     * 
     * @return Flow<Resource<List<Task>>> dengan tasks yang isDeleted = true
     */
    fun getDeletedTasks(): Flow<Resource<List<Task>>> = callbackFlow {
        trySend(Resource.Loading())
        
        val currentUserId = getCurrentUserId()
        
        if (currentUserId == null) {
            trySend(Resource.Error("User belum login"))
            close()
            return@callbackFlow
        }

        val listener = tasksCollection
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("isDeleted", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Terjadi kesalahan"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    try {
                        val tasks = snapshot.documents
                            .mapNotNull { documentToTask(it) }
                            .sortedByDescending { it.timestamp }
                        
                        trySend(Resource.Success(tasks))
                    } catch (e: Exception) {
                        trySend(Resource.Error(e.message ?: "Gagal memproses data"))
                    }
                }
            }

        awaitClose { listener.remove() }
    }

    // ==================== CREATE OPERATIONS ====================

    /**
     * Menambahkan task baru ke Firestore.
     * 
     * @param task Task object yang akan ditambahkan (id akan di-generate oleh Firestore)
     * @throws IllegalStateException jika user belum login
     */
    suspend fun addTask(task: Task) {
        try {
            val currentUserId = getCurrentUserId()
                ?: throw IllegalStateException("User belum login")

            val newTask = task.copy(
                userId = currentUserId,
                timestamp = System.currentTimeMillis()
            )
            
            tasksCollection.add(newTask).await()
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Menambahkan task baru dengan parameter individual.
     * 
     * @param title Judul task
     * @param priority Prioritas: Low, Medium, High
     * @param category Kategori: Work, Study, Personal, Others
     * @param dueDate Deadline dalam milliseconds (nullable)
     */
    suspend fun addTask(title: String, priority: String, category: String, dueDate: Long?) {
        val task = Task(
            title = title,
            priority = priority,
            category = category,
            dueDate = dueDate,
            isCompleted = false,
            isPinned = false,
            isDeleted = false
        )
        addTask(task)
    }

    // ==================== UPDATE OPERATIONS ====================

    /**
     * Mengupdate task yang sudah ada.
     * 
     * @param task Task dengan data yang sudah diupdate (harus memiliki id valid)
     */
    suspend fun updateTask(task: Task) {
        try {
            if (task.id.isEmpty()) {
                throw IllegalArgumentException("Task ID tidak boleh kosong")
            }
            
            tasksCollection.document(task.id)
                .set(task)
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Mengupdate status penyelesaian task.
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
     * Toggle status pin task.
     * 
     * @param taskId ID task yang akan di-toggle
     * @param isPinned Status pin baru
     */
    suspend fun togglePin(taskId: String, isPinned: Boolean) {
        try {
            tasksCollection.document(taskId)
                .update("isPinned", isPinned)
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Toggle status pin task berdasarkan status saat ini.
     * 
     * @param taskId ID task
     * @param currentStatus Status pin saat ini (akan di-invert)
     */
    suspend fun togglePinStatus(taskId: String, currentStatus: Boolean) {
        togglePin(taskId, !currentStatus)
    }

    // ==================== DELETE OPERATIONS ====================

    /**
     * Menghapus task secara permanen dari Firestore.
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

    /**
     * Soft delete - Memindahkan task ke recycle bin.
     * Task tidak dihapus permanen, hanya menandai isDeleted = true.
     * 
     * @param taskId ID task yang akan dipindahkan ke recycle bin
     */
    suspend fun softDeleteTask(taskId: String) {
        try {
            tasksCollection.document(taskId)
                .update("isDeleted", true)
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Mengembalikan task dari recycle bin.
     * Menandai isDeleted = false.
     * 
     * @param taskId ID task yang akan dikembalikan
     */
    suspend fun restoreTask(taskId: String) {
        try {
            tasksCollection.document(taskId)
                .update("isDeleted", false)
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Alias untuk deleteTask - Menghapus task secara permanen.
     * 
     * @param taskId ID task yang akan dihapus permanen
     */
    suspend fun deleteTaskPermanently(taskId: String) {
        deleteTask(taskId)
    }
}
