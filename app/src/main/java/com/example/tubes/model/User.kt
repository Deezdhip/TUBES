package com.example.tubes.model

/**
 * Data class untuk User Profile di Firestore.
 * 
 * @property uid User ID dari Firebase Authentication
 * @property name Nama lengkap user
 * @property email Email user
 * @property createdAt Waktu registrasi
 */
data class User(
    var uid: String = "",
    var name: String = "",
    var email: String = "",
    var createdAt: Long = System.currentTimeMillis()
) {
    // Empty constructor untuk Firestore
    constructor() : this("", "", "", System.currentTimeMillis())
}
