package com.example.tubes.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility object untuk operasi terkait tanggal dan waktu.
 */
object DateUtils {
    
    private const val DATE_FORMAT_PATTERN = "dd MMM yyyy, HH:mm"
    
    /**
     * Formats a timestamp (Long) into a readable date string.
     * Format: "dd MMM yyyy, HH:mm" (e.g., "15 Dec 2025, 14:30")
     * 
     * @param timestamp The timestamp to format. Can be null.
     * @return Formatted string or empty string if timestamp is null.
     */
    fun formatDate(timestamp: Long?): String {
        if (timestamp == null) return ""
        
        val date = Date(timestamp)
        val format = SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.getDefault())
        return format.format(date)
    }
    
    /**
     * Alias untuk formatDate - untuk kompatibilitas dengan kode lama.
     * Format: "dd MMM yyyy, HH:mm" (e.g., "15 Dec 2025, 14:30")
     * 
     * @param timestamp The timestamp to format. Can be null.
     * @return Formatted string or empty string if timestamp is null.
     */
    fun formatDeadline(timestamp: Long?): String = formatDate(timestamp)

    /**
     * Checks if a deadline is overdue.
     * 
     * @param dueDate The deadline timestamp.
     * @return True if current time > dueDate.
     */
    fun isOverdue(dueDate: Long?): Boolean {
        if (dueDate == null) return false
        return System.currentTimeMillis() > dueDate
    }
}

