package com.example.tubes.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubes.model.Task
import com.example.tubes.repository.TaskRepository
import com.example.tubes.util.DateUtils
import com.example.tubes.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ==================== UI STATE ====================

/**
 * Sealed class untuk merepresentasikan berbagai state UI
 */
sealed class TaskUiState {
    /**
     * State loading saat data sedang diambil
     */
    object Loading : TaskUiState()
    
    /**
     * State sukses dengan list tasks
     */
    data class Success(val tasks: List<Task>) : TaskUiState()
    
    /**
     * State error dengan pesan error
     */
    data class Error(val message: String) : TaskUiState()
}

// ==================== VIEW MODEL ====================

/**
 * ViewModel untuk mengelola state dan logic Task.
 * Menggunakan StateFlow untuk reactive UI updates.
 * Implementasi Clean Architecture dengan Optimistic Updates.
 */
class TaskViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "TaskViewModel"
    }

    // ==================== DEPENDENCIES ====================
    
    private val repository = TaskRepository()

    // ==================== STATE FLOWS ====================
    
    /**
     * StateFlow untuk menampung UI state tasks utama
     */
    private val _uiState = MutableStateFlow<TaskUiState>(TaskUiState.Loading)
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    /**
     * StateFlow untuk menampung deleted tasks (Recycle Bin)
     */
    private val _deletedTasksState = MutableStateFlow<TaskUiState>(TaskUiState.Loading)
    val deletedTasksState: StateFlow<TaskUiState> = _deletedTasksState.asStateFlow()

    /**
     * StateFlow untuk error messages yang bisa di-dismiss
     */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ==================== SORTING COMPARATOR ====================
    
    /**
     * Comparator untuk sorting tasks.
     * Urutan prioritas:
     * 1. isPinned (desc) - Pinned tasks di atas
     * 2. isOverdue (desc) - Overdue tasks (belum selesai) diprioritaskan
     * 3. isCompleted (asc) - Belum selesai di atas
     * 4. dueDate (asc, nulls last) - Deadline terdekat di atas
     * 5. timestamp (desc) - Terbaru di atas
     */
    private val taskComparator: Comparator<Task> = compareByDescending<Task> { it.isPinned }
        .thenByDescending { !it.isCompleted && DateUtils.isOverdue(it.dueDate) } // Overdue di atas
        .thenBy { it.isCompleted }
        .thenBy(nullsLast()) { it.dueDate }
        .thenByDescending { it.timestamp }

    // ==================== INITIALIZATION ====================
    
    init {
        loadTasks()
        loadDeletedTasks()
    }

    /**
     * Memuat tasks dari repository dengan real-time updates
     */
    private fun loadTasks() {
        viewModelScope.launch {
            repository.getTasks().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = TaskUiState.Loading
                    }
                    is Resource.Success -> {
                        _uiState.value = TaskUiState.Success(resource.data ?: emptyList())
                    }
                    is Resource.Error -> {
                        _uiState.value = TaskUiState.Error(resource.message ?: "Terjadi kesalahan")
                    }
                }
            }
        }
    }

    /**
     * Memuat deleted tasks untuk Recycle Bin
     */
    private fun loadDeletedTasks() {
        viewModelScope.launch {
            repository.getDeletedTasks().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _deletedTasksState.value = TaskUiState.Loading
                    }
                    is Resource.Success -> {
                        _deletedTasksState.value = TaskUiState.Success(resource.data ?: emptyList())
                    }
                    is Resource.Error -> {
                        _deletedTasksState.value = TaskUiState.Error(resource.message ?: "Terjadi kesalahan")
                        Log.e(TAG, "Error fetching deleted tasks: ${resource.message}")
                    }
                }
            }
        }
    }

    // ==================== CREATE OPERATIONS ====================

    /**
     * Menambahkan task baru dengan parameter lengkap
     * 
     * @param title Judul task
     * @param priority Prioritas: Low, Medium, High
     * @param category Kategori: Work, Study, Personal, Others
     * @param dueDate Deadline dalam milliseconds (nullable)
     */
    fun addTask(title: String, priority: String, category: String, dueDate: Long?) {
        if (title.isBlank()) {
            Log.d(TAG, "Attempting to add task with blank title")
            _errorMessage.value = "Judul task tidak boleh kosong"
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Adding task: $title, priority: $priority, category: $category, due: $dueDate")
                repository.addTask(title, priority, category, dueDate)
                // State akan otomatis update via real-time listener
            } catch (e: Exception) {
                Log.e(TAG, "Error adding task", e)
                _errorMessage.value = e.message ?: "Gagal menambahkan task"
            }
        }
    }

    /**
     * Menambahkan task baru dengan Task object
     * 
     * @param task Task object yang akan ditambahkan
     */
    fun addTask(task: Task) {
        if (task.title.isBlank()) {
            _errorMessage.value = "Judul task tidak boleh kosong"
            return
        }

        viewModelScope.launch {
            try {
                repository.addTask(task)
            } catch (e: Exception) {
                Log.e(TAG, "Error adding task", e)
                _errorMessage.value = e.message ?: "Gagal menambahkan task"
            }
        }
    }

    // ==================== UPDATE OPERATIONS (OPTIMISTIC) ====================

    /**
     * Mengupdate status penyelesaian task dengan Optimistic Update
     * UI diupdate langsung, kemudian sync ke server.
     * Jika gagal, state akan di-rollback.
     * 
     * @param taskId ID task yang akan diupdate
     * @param isCompleted Status baru
     */
    fun updateTaskStatus(taskId: String, isCompleted: Boolean) {
        val currentState = _uiState.value
        if (currentState !is TaskUiState.Success) return
        
        val originalList = currentState.tasks
        
        // Step 1: Optimistic Update - Update UI terlebih dahulu
        val updatedList = originalList.map { task ->
            if (task.id == taskId) task.copy(isCompleted = isCompleted) else task
        }.sortedWith(taskComparator)
        
        _uiState.value = TaskUiState.Success(updatedList)
        Log.d(TAG, "Optimistic Update: Status changed for $taskId to $isCompleted")

        // Step 2: Sync ke Server
        viewModelScope.launch {
            try {
                repository.updateTaskStatus(taskId, isCompleted)
                // Success - state sudah benar dari optimistic update
            } catch (e: Exception) {
                // Step 3: Rollback jika gagal
                Log.e(TAG, "Sync Failed, Rolling back", e)
                _uiState.value = TaskUiState.Success(originalList)
                _errorMessage.value = "Gagal mengupdate status: ${e.message}"
            }
        }
    }

    /**
     * Toggle status pin task dengan Optimistic Update
     * UI diupdate langsung, kemudian sync ke server.
     * Jika gagal, state akan di-rollback.
     *
     * @param task Task yang akan dipin/unpin
     */
    fun togglePin(task: Task) {
        val currentState = _uiState.value
        if (currentState !is TaskUiState.Success) return
        
        val originalList = currentState.tasks
        val newPinStatus = !task.isPinned
        
        // Step 1: Optimistic Update dengan client-side sort
        val updatedList = originalList.map { t ->
            if (t.id == task.id) t.copy(isPinned = newPinStatus) else t
        }.sortedWith(taskComparator)
        
        _uiState.value = TaskUiState.Success(updatedList)
        Log.d(TAG, "Optimistic Update: Pin changed for ${task.id} to $newPinStatus")

        // Step 2: Sync ke Server
        viewModelScope.launch {
            try {
                repository.togglePin(task.id, newPinStatus)
                // Success - state sudah benar dari optimistic update
            } catch (e: Exception) {
                // Step 3: Rollback jika gagal
                Log.e(TAG, "Sync Failed, Rolling back", e)
                _uiState.value = TaskUiState.Success(originalList)
                _errorMessage.value = "Gagal mengubah pin: ${e.message}"
            }
        }
    }

    // ==================== DELETE OPERATIONS ====================

    /**
     * Soft delete - Memindahkan task ke recycle bin
     * 
     * @param taskId ID task yang akan dipindahkan ke recycle bin
     */
    fun deleteTask(taskId: String) {
        val currentState = _uiState.value
        if (currentState !is TaskUiState.Success) return
        
        val originalList = currentState.tasks
        
        // Optimistic Update - hapus dari list
        val updatedList = originalList.filter { it.id != taskId }
        _uiState.value = TaskUiState.Success(updatedList)

        viewModelScope.launch {
            try {
                repository.softDeleteTask(taskId)
            } catch (e: Exception) {
                // Rollback
                Log.e(TAG, "Soft delete failed", e)
                _uiState.value = TaskUiState.Success(originalList)
                _errorMessage.value = "Gagal menghapus task: ${e.message}"
            }
        }
    }

    /**
     * Mengembalikan task dari recycle bin
     * 
     * @param task Task yang akan dikembalikan
     */
    fun restoreTask(task: Task) {
        viewModelScope.launch {
            try {
                repository.restoreTask(task.id)
                // State akan diupdate via real-time listener
            } catch (e: Exception) {
                Log.e(TAG, "Restore failed", e)
                _errorMessage.value = "Gagal memulihkan task: ${e.message}"
            }
        }
    }

    /**
     * Menghapus task secara permanen
     * 
     * @param taskId ID task yang akan dihapus permanen
     */
    fun deleteTaskPermanently(taskId: String) {
        val currentState = _deletedTasksState.value
        if (currentState is TaskUiState.Success) {
            val originalList = currentState.tasks
            
            // Optimistic Update
            val updatedList = originalList.filter { it.id != taskId }
            _deletedTasksState.value = TaskUiState.Success(updatedList)

            viewModelScope.launch {
                try {
                    repository.deleteTaskPermanently(taskId)
                } catch (e: Exception) {
                    // Rollback
                    Log.e(TAG, "Permanent delete failed", e)
                    _deletedTasksState.value = TaskUiState.Success(originalList)
                    _errorMessage.value = "Gagal menghapus permanen: ${e.message}"
                }
            }
        }
    }

    // ==================== UTILITY FUNCTIONS ====================

    /**
     * Reset error state dan trigger reload data
     */
    fun clearError() {
        _errorMessage.value = null
        val currentState = _uiState.value
        if (currentState is TaskUiState.Error) {
            loadTasks()
        }
    }

    /**
     * Dismiss error message
     */
    fun dismissError() {
        _errorMessage.value = null
    }

    /**
     * Refresh data - reload dari repository
     */
    fun refresh() {
        loadTasks()
        loadDeletedTasks()
    }
}
