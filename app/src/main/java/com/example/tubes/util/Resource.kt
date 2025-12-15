package com.example.tubes.util

/**
 * Sealed class untuk menangani state dari operasi async.
 * Digunakan untuk membungkus hasil dari repository operations.
 * 
 * @param T Tipe data yang dibungkus
 * @property data Data hasil operasi (nullable)
 * @property message Pesan error jika ada (nullable)
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    /**
     * State sukses dengan data
     */
    class Success<T>(data: T) : Resource<T>(data)
    
    /**
     * State error dengan pesan error dan data opsional
     */
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    
    /**
     * State loading saat operasi sedang berjalan
     */
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
