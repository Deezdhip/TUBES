package com.example.tubes.model

import android.os.Parcelable
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

/**
 * Data class untuk merepresentasikan Task di Firestore.
 * 
 * PENTING: Menggunakan @PropertyName annotation untuk memastikan
 * Firestore membaca nama field yang persis, bukan menebak via JavaBean convention.
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
 * @property progress Progress task 0.0 - 1.0
 * @property focusTimeSpent Total waktu fokus yang dihabiskan (dalam DETIK)
 */
@Parcelize
data class Task(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    
    // Firestore PropertyName untuk boolean fields
    @get:PropertyName("isCompleted") 
    @set:PropertyName("isCompleted")
    var isCompleted: Boolean = false,
    
    @get:PropertyName("isPinned") 
    @set:PropertyName("isPinned")
    var isPinned: Boolean = false,
    
    @get:PropertyName("isDeleted") 
    @set:PropertyName("isDeleted")
    var isDeleted: Boolean = false,
    
    val dueDate: Long? = null,
    val priority: String = "Medium",
    val category: String = "Personal",
    val timestamp: Long = System.currentTimeMillis(),
    val progress: Float = 0f,
    
    // Field baru untuk menyimpan durasi fokus asli (dalam DETIK)
    @get:PropertyName("focusTimeSpent") 
    @set:PropertyName("focusTimeSpent")
    var focusTimeSpent: Long = 0
) : Parcelable
