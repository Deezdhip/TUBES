package com.example.tubes.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubes.model.Task
import com.example.tubes.repository.TaskRepository
import com.example.tubes.util.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Enum untuk state timer
 */
enum class TimerState {
    IDLE, RUNNING, PAUSED
}

/**
 * Data class untuk UI state timer
 */
data class TimerUiState(
    val timerState: TimerState = TimerState.IDLE,
    val timeLeftInSeconds: Int = 25 * 60, // Default 25 menit
    val totalTimeInSeconds: Int = 25 * 60,
    val selectedDurationInSeconds: Int = 25 * 60,
    val selectedSound: String = "Off",
    val timerCompleted: Boolean = false, // Flag untuk trigger completion event
    val isTimerFinished: Boolean = false, // Flag untuk menampilkan tombol "Selesai"
    // Task info
    val taskId: String = "",
    val taskTitle: String = "Focus Session",
    val taskCategory: String = "",
    val isLoadingTask: Boolean = false,
    val isCompletingTask: Boolean = false // Flag saat proses complete task
)

/**
 * ViewModel untuk mengelola state dan logic Timer.
 * Menggunakan StateFlow untuk reactive UI updates.
 * 
 * Features:
 * - Timer countdown dengan coroutine
 * - Proper cleanup dengan onCleared()
 * - Timer completion event untuk vibrate/notifikasi
 * - Load task by ID for task-specific timer
 */
class TimerViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "TimerViewModel"
    }
    
    private val repository = TaskRepository()
    
    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()
    
    // Job untuk timer coroutine - akan di-cancel saat ViewModel dihancurkan
    private var timerJob: Job? = null
    
    /**
     * Load task by ID dari repository
     * Akan mengisi taskTitle dan taskCategory di UI state
     */
    fun loadTask(taskId: String) {
        if (taskId.isBlank()) {
            Log.d(TAG, "No taskId provided, using default Focus Session")
            return
        }
        
        _uiState.value = _uiState.value.copy(
            taskId = taskId,
            isLoadingTask = true
        )
        
        viewModelScope.launch {
            try {
                repository.getTaskById(taskId)?.let { task ->
                    Log.d(TAG, "Loaded task: ${task.title}")
                    _uiState.value = _uiState.value.copy(
                        taskTitle = task.title,
                        taskCategory = task.category,
                        isLoadingTask = false
                    )
                } ?: run {
                    Log.w(TAG, "Task not found with id: $taskId")
                    _uiState.value = _uiState.value.copy(
                        taskTitle = "Focus Session",
                        isLoadingTask = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading task", e)
                _uiState.value = _uiState.value.copy(
                    taskTitle = "Focus Session",
                    isLoadingTask = false
                )
            }
        }
    }
    
    /**
     * Set durasi timer dalam menit
     */
    fun setDuration(minutes: Int, seconds: Int = 0) {
        val totalSeconds = (minutes * 60) + seconds
        if (totalSeconds > 0 && _uiState.value.timerState == TimerState.IDLE) {
            _uiState.value = _uiState.value.copy(
                selectedDurationInSeconds = totalSeconds,
                totalTimeInSeconds = totalSeconds,
                timeLeftInSeconds = totalSeconds
            )
        }
    }
    
    /**
     * Set durasi timer dalam detik
     */
    fun setDurationInSeconds(totalSeconds: Int) {
        if (totalSeconds > 0 && _uiState.value.timerState == TimerState.IDLE) {
            _uiState.value = _uiState.value.copy(
                selectedDurationInSeconds = totalSeconds,
                totalTimeInSeconds = totalSeconds,
                timeLeftInSeconds = totalSeconds
            )
        }
    }
    
    /**
     * Set selected ambient sound
     */
    fun setSelectedSound(sound: String) {
        _uiState.value = _uiState.value.copy(selectedSound = sound)
    }
    
    /**
     * Start atau resume timer
     */
    fun startTimer() {
        if (_uiState.value.timerState == TimerState.RUNNING) return
        
        _uiState.value = _uiState.value.copy(
            timerState = TimerState.RUNNING,
            timerCompleted = false
        )
        
        startCountdown()
    }
    
    /**
     * Pause timer
     */
    fun pauseTimer() {
        if (_uiState.value.timerState == TimerState.RUNNING) {
            timerJob?.cancel()
            _uiState.value = _uiState.value.copy(timerState = TimerState.PAUSED)
        }
    }
    
    /**
     * Toggle antara start dan pause
     */
    fun toggleTimer() {
        when (_uiState.value.timerState) {
            TimerState.IDLE, TimerState.PAUSED -> startTimer()
            TimerState.RUNNING -> pauseTimer()
        }
    }
    
    /**
     * Reset timer ke state awal
     */
    fun resetTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            timerState = TimerState.IDLE,
            timeLeftInSeconds = _uiState.value.totalTimeInSeconds,
            timerCompleted = false
        )
    }
    
    /**
     * Internal function untuk menjalankan countdown
     */
    private fun startCountdown() {
        timerJob?.cancel() // Cancel job sebelumnya jika ada
        
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeftInSeconds > 0 && _uiState.value.timerState == TimerState.RUNNING) {
                delay(1000L)
                
                val newTimeLeft = _uiState.value.timeLeftInSeconds - 1
                _uiState.value = _uiState.value.copy(timeLeftInSeconds = newTimeLeft)
                
                // Timer selesai!
                if (newTimeLeft == 0) {
                    onTimerFinished()
                }
            }
        }
    }
    
    /**
     * Called when timer reaches zero
     * Sets isTimerFinished = true to show "Complete" button
     */
    private fun onTimerFinished() {
        _uiState.value = _uiState.value.copy(
            timerState = TimerState.IDLE,
            timerCompleted = true,
            isTimerFinished = true // <-- Show "Selesai" button
        )
    }
    
    /**
     * Trigger timer completion event (vibrate, notification, sound)
     * Dipanggil dari UI setelah mendeteksi timerCompleted = true
     */
    fun triggerCompletionFeedback(context: Context, taskTitle: String) {
        NotificationHelper.onTimerComplete(context, taskTitle)
    }
    
    /**
     * Reset completion flag setelah event ditangani
     */
    fun clearCompletionFlag() {
        _uiState.value = _uiState.value.copy(timerCompleted = false)
    }
    
    /**
     * Complete the current task - Update progress to 100% and mark as completed.
     * Called when user clicks "Selesai" button after timer finishes.
     * 
     * @param onSuccess Callback yang dipanggil setelah task berhasil di-complete
     */
    fun completeTask(onSuccess: () -> Unit = {}) {
        val taskId = _uiState.value.taskId
        
        if (taskId.isBlank()) {
            Log.w(TAG, "Cannot complete task: No taskId available")
            onSuccess() // Still navigate back even if no task
            return
        }
        
        _uiState.value = _uiState.value.copy(isCompletingTask = true)
        
        viewModelScope.launch {
            try {
                // Get current task from repository
                val currentTask = repository.getTaskById(taskId)
                
                if (currentTask != null) {
                    // Update task to completed with 100% progress
                    val completedTask = currentTask.copy(
                        progress = 1.0f,
                        isCompleted = true
                    )
                    repository.updateTask(completedTask)
                    Log.d(TAG, "Task '${currentTask.title}' marked as completed!")
                } else {
                    Log.w(TAG, "Task not found for completion: $taskId")
                }
                
                _uiState.value = _uiState.value.copy(
                    isCompletingTask = false,
                    isTimerFinished = false // Reset flag
                )
                
                onSuccess()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error completing task", e)
                _uiState.value = _uiState.value.copy(isCompletingTask = false)
                onSuccess() // Navigate back anyway
            }
        }
    }
    
    /**
     * Reset timer finished state (for restarting)
     */
    fun resetTimerFinished() {
        _uiState.value = _uiState.value.copy(isTimerFinished = false)
    }
    
    /**
     * Get progress untuk circular progress indicator (1.0 = penuh, 0.0 = habis)
     */
    fun getProgress(): Float {
        val total = _uiState.value.totalTimeInSeconds
        if (total == 0) return 1f
        return _uiState.value.timeLeftInSeconds.toFloat() / total.toFloat()
    }
    
    /**
     * Cleanup saat ViewModel dihancurkan.
     * PENTING: Cancel timer job untuk mencegah memory leak!
     */
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        timerJob = null
    }
}
