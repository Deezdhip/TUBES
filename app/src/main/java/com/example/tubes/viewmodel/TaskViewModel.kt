package com.example.tubes.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubes.model.Task
import com.example.tubes.repository.TaskRepository
import com.example.tubes.util.DateUtils
import com.example.tubes.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
 * Menggunakan StateFlow + combine untuk reactive UI updates dengan INSTANT DELETE.
 * Implementasi Clean Architecture dengan Optimistic Updates via Temporary Hidden List.
 */
class TaskViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "TaskViewModel"
    }

    // ==================== DEPENDENCIES ====================
    
    private val repository = TaskRepository()

    // ==================== INSTANT DELETE: TEMPORARY HIDDEN LIST ====================
    
    /**
     * Set ID tugas yang disembunyikan secara LOKAL (belum sync ke server).
     * Digunakan untuk INSTANT optimistic delete - task hilang 0 detik.
     */
    private val _instantlyHiddenIds = MutableStateFlow<Set<String>>(emptySet())
    
    /**
     * Task yang baru saja dihapus (untuk Undo functionality)
     */
    private val _lastDeletedTask = MutableStateFlow<Task?>(null)
    val lastDeletedTask: StateFlow<Task?> = _lastDeletedTask.asStateFlow()

    // ==================== STATE FLOWS ====================
    
    /**
     * StateFlow untuk menampung raw data dari repository
     */
    private val _rawTasksState = MutableStateFlow<TaskUiState>(TaskUiState.Loading)
    
    /**
     * COMBINED UI STATE: Menggabungkan data dari DB + filter hidden IDs.
     * Task yang ada di _instantlyHiddenIds akan LANGSUNG hilang dari UI.
     */
    val uiState: StateFlow<TaskUiState> = combine(
        _rawTasksState,
        _instantlyHiddenIds
    ) { rawState, hiddenIds ->
        when (rawState) {
            is TaskUiState.Loading -> TaskUiState.Loading
            is TaskUiState.Error -> rawState
            is TaskUiState.Success -> {
                val filteredTasks = rawState.tasks
                    .filter { !it.isDeleted }           // 1. Filter yang sudah resmi dihapus di DB
                    .filter { it.id !in hiddenIds }     // 2. Filter yang BARU SAJA dihapus (Local/Instant)
                    .sortedWith(taskComparator)
                TaskUiState.Success(filteredTasks)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskUiState.Loading
    )

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
        .thenByDescending { !it.isCompleted && DateUtils.isOverdue(it.dueDate) }
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
                        _rawTasksState.value = TaskUiState.Loading
                    }
                    is Resource.Success -> {
                        val tasks = resource.data ?: emptyList()
                        _rawTasksState.value = TaskUiState.Success(tasks)
                        
                        // PENTING: Clear hidden IDs yang sudah ter-sync ke server
                        // Jika task sudah isDeleted=true di server, hapus dari hidden list
                        val deletedIds = tasks.filter { it.isDeleted }.map { it.id }.toSet()
                        _instantlyHiddenIds.value = _instantlyHiddenIds.value - deletedIds
                    }
                    is Resource.Error -> {
                        _rawTasksState.value = TaskUiState.Error(resource.message ?: "Terjadi kesalahan")
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
            } catch (e: Exception) {
                Log.e(TAG, "Error adding task", e)
                _errorMessage.value = e.message ?: "Gagal menambahkan task"
            }
        }
    }

    /**
     * Menambahkan task baru dengan Task object
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
     */
    fun updateTaskStatus(taskId: String, isCompleted: Boolean) {
        val currentState = _rawTasksState.value
        if (currentState !is TaskUiState.Success) return
        
        val originalList = currentState.tasks
        
        // Optimistic Update
        val updatedList = originalList.map { task ->
            if (task.id == taskId) task.copy(isCompleted = isCompleted) else task
        }
        
        _rawTasksState.value = TaskUiState.Success(updatedList)
        Log.d(TAG, "Optimistic Update: Status changed for $taskId to $isCompleted")

        viewModelScope.launch {
            try {
                repository.updateTaskStatus(taskId, isCompleted)
            } catch (e: Exception) {
                Log.e(TAG, "Sync Failed, Rolling back", e)
                _rawTasksState.value = TaskUiState.Success(originalList)
                _errorMessage.value = "Gagal mengupdate status: ${e.message}"
            }
        }
    }

    /**
     * TOGGLE TASK STATUS dengan Optimistic Update
     */
    fun toggleTaskStatus(task: Task) {
        val currentState = _rawTasksState.value
        if (currentState !is TaskUiState.Success) return
        
        val originalList = currentState.tasks
        val newStatus = !task.isCompleted
        val newProgress = if (newStatus) 1.0f else 0.0f
        
        Log.d(TAG, "Toggling task '${task.title}': isCompleted $newStatus, progress $newProgress")
        
        // Optimistic Update
        val updatedList = originalList.map { t ->
            if (t.id == task.id) t.copy(
                isCompleted = newStatus,
                progress = newProgress
            ) else t
        }
        
        _rawTasksState.value = TaskUiState.Success(updatedList)

        viewModelScope.launch {
            try {
                val updatedTask = task.copy(
                    isCompleted = newStatus,
                    progress = newProgress
                )
                repository.updateTask(updatedTask)
                Log.d(TAG, "Task updated in Firebase: ${task.title}")
            } catch (e: Exception) {
                Log.e(TAG, "Toggle failed, rolling back", e)
                _rawTasksState.value = TaskUiState.Success(originalList)
                _errorMessage.value = "Gagal mengupdate task: ${e.message}"
            }
        }
    }

    /**
     * Toggle status pin task dengan Optimistic Update
     */
    fun togglePin(task: Task) {
        val currentState = _rawTasksState.value
        if (currentState !is TaskUiState.Success) return
        
        val originalList = currentState.tasks
        val newPinStatus = !task.isPinned
        
        // Optimistic Update
        val updatedList = originalList.map { t ->
            if (t.id == task.id) t.copy(isPinned = newPinStatus) else t
        }
        
        _rawTasksState.value = TaskUiState.Success(updatedList)
        Log.d(TAG, "Optimistic Update: Pin changed for ${task.id} to $newPinStatus")

        viewModelScope.launch {
            try {
                repository.togglePin(task.id, newPinStatus)
            } catch (e: Exception) {
                Log.e(TAG, "Sync Failed, Rolling back", e)
                _rawTasksState.value = TaskUiState.Success(originalList)
                _errorMessage.value = "Gagal mengubah pin: ${e.message}"
            }
        }
    }

    // ==================== DELETE OPERATIONS (INSTANT) ====================

    /**
     * INSTANT DELETE - Memindahkan task ke recycle bin dengan 0 DETIK delay.
     * Menggunakan Temporary Hidden List pattern.
     * 
     * LANGKAH 1: Sembunyikan visual seketika (Optimistic via _instantlyHiddenIds)
     * LANGKAH 2: Kirim perintah ke server (Background)
     * 
     * @param taskId ID task yang akan dipindahkan ke recycle bin
     */
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            // LANGKAH 1: Sembunyikan visual SEKETIKA (0 detik)
            _instantlyHiddenIds.value = _instantlyHiddenIds.value + taskId
            Log.d(TAG, "INSTANT DELETE: Task $taskId hidden immediately")
            
            // Simpan task untuk Undo
            val currentState = _rawTasksState.value
            if (currentState is TaskUiState.Success) {
                _lastDeletedTask.value = currentState.tasks.find { it.id == taskId }
            }

            // LANGKAH 2: Kirim perintah ke server (Background)
            try {
                repository.softDeleteTask(taskId)
                Log.d(TAG, "Task $taskId successfully synced to trash in Firebase")
            } catch (e: Exception) {
                // Rollback: Munculkan kembali jika gagal sync
                Log.e(TAG, "Sync to trash failed, rolling back", e)
                _instantlyHiddenIds.value = _instantlyHiddenIds.value - taskId
                _errorMessage.value = "Gagal menghapus task: ${e.message}"
            }
        }
    }

    /**
     * INSTANT DELETE dengan Task object.
     * Alias untuk deleteTask(taskId).
     * 
     * @param task Task yang akan dipindahkan ke recycle bin
     */
    fun moveToTrash(task: Task) {
        deleteTask(task.id)
    }

    /**
     * UNDO DELETE - Membatalkan penghapusan yang baru saja dilakukan.
     * Mengembalikan task ke list aktif.
     * 
     * @param task Task yang akan di-restore (atau null untuk restore lastDeletedTask)
     */
    fun undoDelete(task: Task? = null) {
        val taskToRestore = task ?: _lastDeletedTask.value ?: return
        
        viewModelScope.launch {
            // LANGKAH 1: Munculkan kembali di visual (hapus dari hidden list)
            _instantlyHiddenIds.value = _instantlyHiddenIds.value - taskToRestore.id
            Log.d(TAG, "UNDO DELETE: Task ${taskToRestore.id} restored to view")
            
            // LANGKAH 2: Restore di server
            try {
                repository.restoreTask(taskToRestore.id)
                _lastDeletedTask.value = null
                Log.d(TAG, "Task ${taskToRestore.id} successfully restored in Firebase")
            } catch (e: Exception) {
                Log.e(TAG, "Restore failed", e)
                // Jika gagal restore, sembunyikan lagi
                _instantlyHiddenIds.value = _instantlyHiddenIds.value + taskToRestore.id
                _errorMessage.value = "Gagal membatalkan hapus: ${e.message}"
            }
        }
    }

    /**
     * Mengembalikan task dari recycle bin (untuk RecycleBinScreen)
     */
    fun restoreTask(task: Task) {
        viewModelScope.launch {
            try {
                repository.restoreTask(task.id)
            } catch (e: Exception) {
                Log.e(TAG, "Restore failed", e)
                _errorMessage.value = "Gagal memulihkan task: ${e.message}"
            }
        }
    }

    /**
     * Menghapus task secara permanen dari Recycle Bin
     */
    fun deleteTaskPermanently(taskId: String) {
        val currentState = _deletedTasksState.value
        if (currentState is TaskUiState.Success) {
            val originalList = currentState.tasks
            
            // Optimistic Update untuk recycle bin
            val updatedList = originalList.filter { it.id != taskId }
            _deletedTasksState.value = TaskUiState.Success(updatedList)

            viewModelScope.launch {
                try {
                    repository.deleteTaskPermanently(taskId)
                } catch (e: Exception) {
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
        val currentState = _rawTasksState.value
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
     * Clear last deleted task (setelah snackbar hilang)
     */
    fun clearLastDeletedTask() {
        _lastDeletedTask.value = null
    }

    /**
     * Refresh data - reload dari repository
     */
    fun refresh() {
        loadTasks()
        loadDeletedTasks()
    }
}
