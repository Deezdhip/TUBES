package com.example.tubes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubes.repository.TaskRepository
import com.example.tubes.util.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StatsUiState(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val totalFocusMinutes: Int = 0,
    val tasksByCategory: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
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
            repository.getTasks().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    
                    is Resource.Success -> {
                        val tasks = resource.data ?: emptyList()
                        val completed = tasks.count { it.isCompleted }
                        val byCategory = tasks.groupBy { it.category }
                            .mapValues { it.value.size }
                        
                        _uiState.value = StatsUiState(
                            totalTasks = tasks.size,
                            completedTasks = completed,
                            totalFocusMinutes = completed * 25,
                            tasksByCategory = byCategory,
                            isLoading = false,
                            error = null
                        )
                    }
                    
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = resource.message
                        )
                    }
                }
            }
        }
    }

    fun refresh() {
        observeStats()
    }
}
