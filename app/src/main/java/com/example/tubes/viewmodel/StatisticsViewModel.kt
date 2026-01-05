package com.example.tubes.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubes.model.Task
import com.example.tubes.repository.TaskRepository
import com.example.tubes.util.DateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Data class untuk UI state statistik Dashboard
 */
data class StatsUiState(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val overdueTasks: Int = 0,
    val totalFocusMinutes: Int = 0, // Sekarang dari data asli, bukan hardcoded
    val tasksByCategory: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val completionPercentage: Float = 0f
)

/**
 * ViewModel untuk mengelola statistik Dashboard.
 * 
 * REAL-TIME dengan Firestore!
 * Setiap perubahan di Firestore langsung update ke Dashboard.
 * 
 * FOCUS TIME: Sekarang menggunakan data asli dari focusTimeSpent, bukan hardcoded.
 */
class StatisticsViewModel : ViewModel() {
    private val repository = TaskRepository()

    companion object {
        private const val TAG = "DEBUG_APP"
    }

    /**
     * StateFlow yang langsung terhubung ke Firestore real-time listener.
     */
    val uiState: StateFlow<StatsUiState> = repository.getTasksFlow()
        .map { tasks -> 
            // ============================================
            // DEBUG: Print semua data dari Firebase
            // ============================================
            Log.d(TAG, "========================================")
            Log.d(TAG, "REAL-TIME UPDATE FROM FIRESTORE")
            Log.d(TAG, "Total tasks received: ${tasks.size}")
            Log.d(TAG, "========================================")
            
            tasks.forEach { task ->
                Log.d(TAG, "Task: ${task.title}")
                Log.d(TAG, "  - isCompleted: ${task.isCompleted}")
                Log.d(TAG, "  - progress: ${task.progress}")
                Log.d(TAG, "  - focusTimeSpent: ${task.focusTimeSpent}s")
                Log.d(TAG, "  - isDone: ${isTaskDone(task)}")
            }
            
            val total = tasks.size
            
            // ============================================
            // LOGIKA SAPU JAGAT - SANGAT FLEKSIBEL
            // ============================================
            val completed = tasks.count { task -> isTaskDone(task) }
            val pending = total - completed
            
            // Overdue = belum selesai DAN sudah lewat deadline
            val overdue = tasks.count { task ->
                !isTaskDone(task) && DateUtils.isOverdue(task.dueDate)
            }
            
            // Group by category
            val byCategory = tasks.groupBy { it.category }
                .mapValues { it.value.size }
            
            // Hitung persentase (0-100), aman dari NaN
            val percentage = if (total > 0) {
                (completed.toFloat() / total.toFloat()) * 100f
            } else 0f

            // ============================================
            // REAL-TIME FOCUS TRACKING
            // Hitung total detik dari semua tugas, lalu bagi 60 untuk dapat menit
            // ============================================
            val totalSeconds = tasks.sumOf { it.focusTimeSpent }
            val totalFocusMinutes = (totalSeconds / 60).toInt()
            
            // DEBUG: Log hasil perhitungan
            Log.d(TAG, "----------------------------------------")
            Log.d(TAG, "CALCULATION RESULTS:")
            Log.d(TAG, "  Total: $total")
            Log.d(TAG, "  Completed: $completed")
            Log.d(TAG, "  Pending: $pending")
            Log.d(TAG, "  Percentage: ${percentage}%")
            Log.d(TAG, "  Total Focus Seconds: $totalSeconds")
            Log.d(TAG, "  Total Focus Minutes: $totalFocusMinutes")
            Log.d(TAG, "========================================")
            
            StatsUiState(
                totalTasks = total,
                completedTasks = completed,
                pendingTasks = pending,
                overdueTasks = overdue,
                totalFocusMinutes = totalFocusMinutes, // Sekarang dari data asli!
                tasksByCategory = byCategory,
                isLoading = false,
                error = null,
                completionPercentage = percentage
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = StatsUiState(isLoading = true)
        )

    // ==================== PULL-TO-REFRESH ====================
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    /**
     * Refresh function untuk Pull-to-Refresh.
     * Karena data sudah real-time dari Firestore, ini hanya memberikan visual feedback.
     */
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(1500) // Simulasi refresh visual
            _isRefreshing.value = false
        }
    }

    /**
     * CEK APAKAH TASK SELESAI
     * Logika Sapu Jagat: True jika SALAH SATU kondisi terpenuhi:
     * 1. isCompleted == true
     * 2. progress >= 0.99 (skala 0.0-1.0)
     * 3. progress >= 99 (skala 0-100)
     */
    private fun isTaskDone(task: Task): Boolean {
        // Kondisi 1: Boolean manual
        if (task.isCompleted) return true
        
        val progress = task.progress
        
        // Kondisi 2: Progress skala 0.0-1.0
        if (progress >= 0.99f && progress <= 1.01f) return true
        
        // Kondisi 3: Progress skala 0-100
        if (progress >= 99f) return true
        
        return false
    }

    /**
     * Konversi progress ke skala 0.0-1.0 dengan aman
     */
    private fun safeProgress(task: Task): Float {
        if (task.isCompleted) return 1f
        
        val rawProgress = task.progress
        
        return when {
            rawProgress > 1f -> (rawProgress / 100f).coerceIn(0f, 1f)
            else -> rawProgress.coerceIn(0f, 1f)
        }
    }
}
