package com.example.tubes.model

/**
 * Data class untuk merepresentasikan Task di Firestore.
 * 
 * @property id ID dokumen dari Firestore
 * @property userId ID user pemilik task (untuk multi-user support)
 * @property title Judul task
 * @property isCompleted Status penyelesaian task
 * @property timestamp Waktu pembuatan task untuk sorting
 */
data class Task(
    var id: String = "",
    var userId: String = "",
    var title: String = "",
    var isCompleted: Boolean = false,
    var priority: String = "Medium", // Low, Medium, High
    var category: String = "Personal", // Work, Study, Personal, Others
    var isPinned: Boolean = false,
    var isDeleted: Boolean = false,
    var dueDate: Long? = null,
    var timestamp: Long = System.currentTimeMillis()
) {
    // Empty constructor untuk Firestore
    constructor() : this("", "", "", false, "Medium", "Personal", false, false, null, System.currentTimeMillis())
}
