package com.example.tubes.util

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import java.util.Calendar

/**
 * Utility object untuk menampilkan DatePicker dan TimePicker secara berurutan (chained).
 */
object DateTimePickerUtils {

    /**
     * Menampilkan DatePickerDialog diikuti TimePickerDialog secara berurutan.
     * Setelah user memilih tanggal dan jam, callback akan dipanggil dengan timestamp (Long).
     *
     * @param context Context untuk menampilkan dialog.
     * @param initialTimestamp Timestamp awal untuk pre-fill picker (null = waktu sekarang).
     * @param onDateTimeSelected Callback yang dipanggil dengan hasil timestamp dalam milliseconds.
     */
    fun showDateTimePicker(
        context: Context,
        initialTimestamp: Long? = null,
        onDateTimeSelected: (Long) -> Unit
    ) {
        // Gunakan Calendar untuk menyimpan hasil pilihan
        val calendar = Calendar.getInstance()
        
        // Jika ada initial timestamp, gunakan sebagai nilai awal
        initialTimestamp?.let { calendar.timeInMillis = it }

        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        // Step 1: Tampilkan DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Update calendar dengan tanggal yang dipilih
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, selectedMonth)
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay)

                // Step 2: Setelah tanggal dipilih, LANGSUNG tampilkan TimePickerDialog
                val timePickerDialog = TimePickerDialog(
                    context,
                    { _, selectedHour, selectedMinute ->
                        // Update calendar dengan jam yang dipilih
                        calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                        calendar.set(Calendar.MINUTE, selectedMinute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        // Step 3: Panggil callback dengan hasil timestamp akhir
                        onDateTimeSelected(calendar.timeInMillis)
                    },
                    currentHour,
                    currentMinute,
                    true // Format 24 jam
                )
                timePickerDialog.setTitle("Pilih Jam")
                timePickerDialog.show()
            },
            currentYear,
            currentMonth,
            currentDay
        )
        datePickerDialog.setTitle("Pilih Tanggal")
        datePickerDialog.show()
    }

    /**
     * Versi sederhana tanpa initial timestamp.
     * Menampilkan DatePickerDialog diikuti TimePickerDialog.
     *
     * @param context Context untuk menampilkan dialog.
     * @param onDateTimeSelected Callback yang dipanggil dengan hasil timestamp dalam milliseconds.
     */
    fun showDatePicker(
        context: Context,
        onDateSelected: (Long) -> Unit
    ) {
        showDateTimePicker(context, null, onDateSelected)
    }
}
