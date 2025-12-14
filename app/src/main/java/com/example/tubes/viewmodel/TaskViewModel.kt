package com.example.tubes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.tubes.model.Task
import com.example.tubes.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Sealed class untuk merepresentasikan berbagai state UI
 */
sealed class TaskUiState {
    object Loading : TaskUiState()
    data class Success(val tasks: List<Task>) : TaskUiState()
    data class Error(val message: String) : TaskUiState()
}

/**
 * ViewModel untuk mengelola state dan logic Task.
 * Menggunakan StateFlow untuk reactive UI updates.
 */
class TaskViewModel : ViewModel() {
    private val repository = TaskRepository()

    // StateFlow untuk menampung UI state
    private val _uiState = MutableStateFlow<TaskUiState>(TaskUiState.Loading)
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    private val _deletedTasks = MutableStateFlow<List<Task>>(emptyList())
    val deletedTasks: StateFlow<List<Task>> = _deletedTasks.asStateFlow()

    init {
        observeTasks()
        observeDeletedTasks()
    }

    /**
     * Observe tasks dari repository dengan real-time updates
     */
    private fun observeTasks() {
        viewModelScope.launch {
            _uiState.value = TaskUiState.Loading
            repository.getTasks()
                .catch { error ->
                    _uiState.value = TaskUiState.Error(
                        error.message ?: "Terjadi kesalahan saat mengambil data"
                    )
                }
                .collect { tasks ->
                    _uiState.value = TaskUiState.Success(tasks)
                }
        }
    }

    private fun observeDeletedTasks() {
        viewModelScope.launch {
            repository.getDeletedTasks()
                .catch { e -> 
                    Log.e("TaskViewModel", "Error fetching deleted tasks", e)
                }
                .collect { tasks ->
                    _deletedTasks.value = tasks
                }
        }
    }

    /**
     * Menambahkan task baru
     * 
     * @param title Judul task yang akan ditambahkan
     */
    fun addTask(title: String, priority: String, category: String, dueDate: Long?) {
        if (title.isBlank()) {
            Log.d("TaskViewModel", "Attempting to add task with blank title")
            _uiState.value = TaskUiState.Error("Judul task tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            try {
                Log.d("TaskViewModel", "Adding task: $title, priority: $priority, category: $category, due: $dueDate")
                repository.addTask(title, priority, category, dueDate)
                // State akan otomatis update via real-time listener
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error adding task", e)
                _uiState.value = TaskUiState.Error(
                    e.message ?: "Gagal menambahkan task"
                )
            }
        }
    }

    /**
     * Mengupdate status penyelesaian task
     * 
     * @param taskId ID task yang akan diupdate
     * @param isCompleted Status baru
     */
    fun updateTaskStatus(taskId: String, isCompleted: Boolean) {
        val currentState = _uiState.value
        if (currentState is TaskUiState.Success) {
            val originalList = currentState.tasks
            // Step A: Optimistic Update
            val updatedList = originalList.map {
                if (it.id == taskId) it.copy(isCompleted = isCompleted) else it
            }
            _uiState.value = TaskUiState.Success(updatedList)
            Log.d("TaskViewModel", "Optimistic Update: Status changed for $taskId")

            // Step B: Sync ke Server
            viewModelScope.launch {
                try {
                    repository.updateTaskStatus(taskId, isCompleted)
                } catch (e: Exception) {
                    // Step C: Rollback
                    Log.e("TaskViewModel", "Sync Failed, Rolling back", e)
                    _uiState.value = TaskUiState.Success(originalList)
                    _uiState.value = TaskUiState.Error("Gagal mengupdate status: ${e.message}")
                }
            }
        }
    }

    /**
     * Toggle status pin task
     *
     * @param task Task yang akan dipin/unpin
     */
    fun togglePin(task: Task) {
        val currentState = _uiState.value
        if (currentState is TaskUiState.Success) {
            val originalList = currentState.tasks
            val newPinStatus = !task.isPinned
            
            // Step A: Optimistic Update & Client-side Sort
            val updatedList = originalList.map {
                if (it.id == task.id) it.copy(isPinned = newPinStatus) else it
            }
            val sortedList = updatedList.sortedWith(
                 compareByDescending<Task> { it.isPinned }
                 .thenBy { it.isCompleted }
                 .thenBy(nullsLast()) { it.dueDate }
                 .thenByDescending { it.timestamp }
            )
            
            _uiState.value = TaskUiState.Success(sortedList)
            Log.d("TaskViewModel", "Optimistic Update: Pin changed for ${task.id}")

            // Step B: Sync ke Server
            viewModelScope.launch {
                try {
                    repository.togglePinStatus(task.id, task.isPinned)
                } catch (e: Exception) {
                    // Step C: Rollback
                    Log.e("TaskViewModel", "Sync Failed, Rolling back", e)
                    _uiState.value = TaskUiState.Success(originalList)
                    _uiState.value = TaskUiState.Error("Gagal mengubah pin: ${e.message}")
                }
            }
        }
    }

    /**
     * Menghapus task
     * 
     * @param taskId ID task yang akan dihapus
     */
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                // Change to soft delete
                repository.softDeleteTask(taskId)
            } catch (e: Exception) {
                _uiState.value = TaskUiState.Error(
                    e.message ?: "Gagal menghapus task"
                )
            }
        }
    }

    fun restoreTask(task: Task) {
        viewModelScope.launch {
            try {
                repository.restoreTask(task.id)
            } catch (e: Exception) {
                _uiState.value = TaskUiState.Error(
                    e.message ?: "Gagal memulihkan task"
                )
            }
        }
    }

    fun deleteTaskPermanently(taskId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTaskPermanently(taskId)
            } catch (e: Exception) {
                _uiState.value = TaskUiState.Error(
                    e.message ?: "Gagal menghapus permanen"
                )
            }
        }
    }

    /**
     * Reset error state kembali ke success dengan data yang ada
     */
    fun clearError() {
        val currentState = _uiState.value
        if (currentState is TaskUiState.Error) {
            // Trigger reload
            observeTasks()
        }
    }
}
