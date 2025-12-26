package com.example.tubes.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.tubes.MainActivity
import com.example.tubes.R

/**
 * Helper class untuk mengelola notifikasi dan feedback timer.
 * Mendukung vibration, notification, dan sound saat timer selesai.
 */
object NotificationHelper {
    
    private const val CHANNEL_ID = "timer_channel"
    private const val CHANNEL_NAME = "Timer Notifications"
    private const val CHANNEL_DESCRIPTION = "Notifikasi saat timer fokus selesai"
    private const val NOTIFICATION_ID = 1001
    
    /**
     * Membuat notification channel untuk Android 8.0+
     * Harus dipanggil saat aplikasi dimulai (di MainActivity atau Application)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Menampilkan notifikasi bahwa timer telah selesai.
     * Memainkan suara notifikasi default dan menampilkan push notification.
     * 
     * @param context Context aplikasi
     * @param title Judul task yang dikerjakan
     */
    fun showTimerCompleteNotification(context: Context, title: String = "Waktu Fokus Selesai!") {
        // Check permission untuk Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission belum diberikan, skip notification
                return
            }
        }
        
        // Intent untuk membuka app saat notification di-tap
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE
        )
        
        // Sound default notification
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText("Selamat! Sesi fokus Anda telah selesai. Istirahat sebentar ya!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        // Show notification
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    
    /**
     * Memicu getaran HP selama 1-2 detik.
     * Mendukung berbagai versi Android.
     * 
     * @param context Context aplikasi
     * @param durationMs Durasi getaran dalam milliseconds (default 1500ms)
     */
    fun vibrate(context: Context, durationMs: Long = 1500L) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ menggunakan VibratorManager
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                // Android 11 dan di bawahnya
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0+ menggunakan VibrationEffect
                // Pattern: 0ms delay, 500ms vibrate, 200ms pause, 500ms vibrate, 200ms pause, 500ms vibrate
                val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
                val vibrationEffect = VibrationEffect.createWaveform(pattern, -1) // -1 = no repeat
                vibrator.vibrate(vibrationEffect)
            } else {
                // Legacy API untuk Android < 8.0
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), -1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Play suara notifikasi default sistem.
     * 
     * @param context Context aplikasi
     */
    fun playNotificationSound(context: Context) {
        try {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, defaultSoundUri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Trigger semua feedback untuk timer selesai: vibrate, sound, dan notification.
     * 
     * @param context Context aplikasi
     * @param taskTitle Judul task yang sedang dikerjakan
     */
    fun onTimerComplete(context: Context, taskTitle: String) {
        // 1. Vibrate HP
        vibrate(context)
        
        // 2. Play notification sound
        playNotificationSound(context)
        
        // 3. Show notification
        showTimerCompleteNotification(context, "Waktu Fokus Selesai!")
    }
}
