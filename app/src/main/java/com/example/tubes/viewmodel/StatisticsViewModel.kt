package com.example.tubes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubes.repository.TaskRepository
import com.example.tubes.util.DateUtils
import com.example.tubes.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Data class untuk UI state statistik Dashboard
 */
data class StatsUiState(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val overdueTasks: Int = 0,
    val totalFocusMinutes: Int = 0,
    val tasksByCategory: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val completionPercentage: Float = 0f
)

/**
 * ViewModel untuk mengelola statistik Dashboard.
 * 
 * PENTING: Menggunakan Flow collection untuk REACTIVE updates!
 * Setiap kali data Task berubah di database (tambah/hapus/edit status),
 * Dashboard akan otomatis menghitung ulang statistik dan UI langsung berubah.
 * 
 * FIX: Menambahkan Job tracking untuk mencegah multiple collectors
 * saat refresh() dipanggil berulang kali.
 */
class StatisticsViewModel : ViewModel() {
    private val repository = TaskRepository()
    private val _uiState = MutableStateFlow(StatsUiState(isLoading = true))
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()
    
    // Job tracking untuk observer - mencegah memory leak & multiple collectors
    private var observerJob: Job? = null

    init {
        observeStats()
    }

    /**
     * Observe tasks dari repository secara reaktif.
     * Menggunakan callbackFlow dari Firestore yang akan emit data setiap ada perubahan.
     * 
     * REACTIVE: Tidak perlu refresh manual!
     * Firestore real-time listener akan push updates otomatis.
     */
    private fun observeStats() {
        // Cancel job sebelumnya jika ada untuk mencegah multiple collectors
        observerJob?.cancel()
        
        observerJob = viewModelScope.launch {
            repository.getTasks().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    
                    is Resource.Success -> {
                        val tasks = resource.data ?: emptyList()
                        
                        // Hitung statistik
                        val total = tasks.size
                        val completed = tasks.count { it.isCompleted }
                        val overdue = tasks.count { 
                            !it.isCompleted && DateUtils.isOverdue(it.dueDate) 
                        }
                        val byCategory = tasks.groupBy { it.category }
                            .mapValues { it.value.size }
                        
                        // Hitung persentase completion (real-time!)
                        val percentage = if (total > 0) {
                            (completed.toFloat() / total.toFloat()) * 100f
                        } else 0f
                        
                        _uiState.value = StatsUiState(
                            totalTasks = total,
                            completedTasks = completed,
                            overdueTasks = overdue,
                            totalFocusMinutes = completed * 25, // Asumsi: 25 menit per task selesai
                            tasksByCategory = byCategory,
                            isLoading = false,
                            error = null,
                            completionPercentage = percentage
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

    /**
     * Force refresh statistik.
     * 
     * NOTE: Dalam kebanyakan kasus, ini tidak diperlukan karena
     * repository.getTasks() sudah menggunakan real-time listener.
     * Function ini tetap disediakan untuk kasus edge dimana
     * user ingin force reload data.
     */
    fun refresh() {
        observeStats()
    }
    
    /**
     * Cleanup saat ViewModel dihancurkan.
     * Cancel observer job untuk mencegah memory leak.
     */
    override fun onCleared() {
        super.onCleared()
        observerJob?.cancel()
        observerJob = null
    }
}
