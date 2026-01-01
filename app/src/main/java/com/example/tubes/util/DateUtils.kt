package com.example.tubes.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility object untuk operasi terkait tanggal dan waktu.
 */
object DateUtils {
    
    private const val DATE_TIME_FORMAT_PATTERN = "dd MMM yyyy, HH:mm"
    
    /**
     * Formats a timestamp (Long) into a readable date-time string.
     * Format: "dd MMM yyyy, HH:mm" (e.g., "18 Dec 2025, 14:30")
     * 
     * @param timestamp The timestamp to format in milliseconds. Can be null.
     * @return Formatted string or empty string if timestamp is null.
     */
    fun formatDateTime(timestamp: Long?): String {
        if (timestamp == null) return ""
        
        val date = Date(timestamp)
        val format = SimpleDateFormat(DATE_TIME_FORMAT_PATTERN, Locale.getDefault())
        return format.format(date)
    }
    
    /**
     * Alias untuk formatDateTime - untuk kompatibilitas dengan kode lama.
     * Format: "dd MMM yyyy, HH:mm" (e.g., "18 Dec 2025, 14:30")
     * 
     * @param timestamp The timestamp to format. Can be null.
     * @return Formatted string or empty string if timestamp is null.
     */
    fun formatDate(timestamp: Long?): String = formatDateTime(timestamp)
    
    /**
     * Alias untuk formatDateTime - untuk kompatibilitas dengan kode lama.
     * Format: "dd MMM yyyy, HH:mm" (e.g., "18 Dec 2025, 14:30")
     * 
     * @param timestamp The timestamp to format. Can be null.
     * @return Formatted string or empty string if timestamp is null.
     */
    fun formatDeadline(timestamp: Long?): String = formatDateTime(timestamp)

    /**
     * Checks if a deadline is overdue (past current time).
     * 
     * @param timestamp The deadline timestamp in milliseconds.
     * @return True if timestamp < System.currentTimeMillis(), false if null or not overdue.
     */
    fun isOverdue(timestamp: Long?): Boolean {
        if (timestamp == null) return false
        return timestamp < System.currentTimeMillis()
    }
    
    /**
     * Formats a timestamp to date only format.
     * Format: "MMM dd, yyyy" (e.g., "May 30, 2022")
     * 
     * @param timestamp The timestamp to format. Can be null.
     * @return Formatted date string or null if timestamp is null.
     */
    fun formatDateOnly(timestamp: Long?): String? {
        if (timestamp == null) return null
        
        val date = Date(timestamp)
        val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return format.format(date)
    }
}
