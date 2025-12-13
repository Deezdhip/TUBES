package com.example.tubes.util

import android.content.Context
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.media.AudioManager

/**
 * Utility to manage playing ambient sounds.
 * Note: Since we don't have actual MP3 assets, we will simulate this or use ToneGenerator for now, 
 * or just prepare the structure for when assets are added.
 * For this demo, let's try to assume we might have raw resources or fail gracefully.
 * 
 * UPDATE: To ensure it works without external assets, I'll use ToneGenerator for a "beep" as a placeholder 
 * if no resource is found, or just log it.
 * Ideally, we should add dummy files to res/raw.
 */
class SoundManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    
    fun playSound(soundResId: Int) {
        stopSound()
        try {
            mediaPlayer = MediaPlayer.create(context, soundResId)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopSound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
