package com.example.tubes.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class untuk merepresentasikan Task di Firestore.
 * 
 * @property id ID dokumen dari Firestore (default empty untuk keamanan)
 * @property userId ID user pemilik task (untuk multi-user support)
 * @property title Judul task
 * @property isCompleted Status penyelesaian task
 * @property isPinned Status pin task (ditampilkan di atas)
 * @property isDeleted Status soft delete (untuk recycle bin)
 * @property dueDate Deadline task dalam milliseconds (null jika tidak ada deadline)
 * @property priority Prioritas task: Low, Medium, High
 * @property category Kategori task: Work, Study, Personal, Others
 * @property timestamp Waktu pembuatan task untuk sorting
 */
@Parcelize
data class Task(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val isCompleted: Boolean = false,
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val dueDate: Long? = null,
    val priority: String = "Medium",
    val category: String = "Personal",
    val timestamp: Long = System.currentTimeMillis(),
    val progress: Float = 0f  // Progress 0.0 - 1.0 atau 0 - 100
) : Parcelable
