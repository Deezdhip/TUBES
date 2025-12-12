package com.example.tubes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    init {
        observeTasks()
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

    /**
     * Menambahkan task baru
     * 
     * @param title Judul task yang akan ditambahkan
     */
    fun addTask(title: String) {
        if (title.isBlank()) {
            _uiState.value = TaskUiState.Error("Judul task tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            try {
                repository.addTask(title)
                // State akan otomatis update via real-time listener
            } catch (e: Exception) {
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
        viewModelScope.launch {
            try {
                repository.updateTaskStatus(taskId, isCompleted)
                // State akan otomatis update via real-time listener
            } catch (e: Exception) {
                _uiState.value = TaskUiState.Error(
                    e.message ?: "Gagal mengupdate status task"
                )
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
                repository.deleteTask(taskId)
                // State akan otomatis update via real-time listener
            } catch (e: Exception) {
                _uiState.value = TaskUiState.Error(
                    e.message ?: "Gagal menghapus task"
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
