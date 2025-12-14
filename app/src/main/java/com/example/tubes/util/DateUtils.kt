package com.example.tubes.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    /**
     * Formats a timestamp (Long) into a readable date string.
     * Format: "dd MMM yyyy, HH:mm" (e.g., "15 Dec 2025, 14:30")
     * 
     * @param timestamp The timestamp to format. Can be null.
     * @return Formatted string or empty string if timestamp is null.
     */
    fun formatDeadline(timestamp: Long?): String {
        if (timestamp == null) return ""
        
        val date = Date(timestamp)
        val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return format.format(date)
    }

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
