package com.example.tubes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubes.model.Task
import com.example.tubes.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StatsUiState(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val totalFocusMinutes: Int = 0, // Placeholder, will need real data later
    val tasksByCategory: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = false
)

class StatisticsViewModel : ViewModel() {
    private val repository = TaskRepository()
    private val _uiState = MutableStateFlow(StatsUiState(isLoading = true))
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        observeStats()
    }

    private fun observeStats() {
        viewModelScope.launch {
            repository.getTasks()
                .collect { tasks ->
                    val completed = tasks.count { it.isCompleted }
                    val byCategory = tasks.groupBy { it.category }
                        .mapValues { it.value.size }
                    
                    _uiState.value = StatsUiState(
                        totalTasks = tasks.size,
                        completedTasks = completed,
                        totalFocusMinutes = completed * 25, // Mock calculation: 25 mins per completed task
                        tasksByCategory = byCategory,
                        isLoading = false
                    )
                }
        }
    }
}
