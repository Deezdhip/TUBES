package com.example.tubes.ui.theme

import androidx.compose.ui.graphics.Color

// ==================== PRIMARY COLORS ====================
/**
 * Warna utama: Royal Blue yang tegas
 */
val PrimaryBlue = Color(0xFF2563EB)

/**
 * Versi sedikit lebih gelap untuk gradient/tekan
 */
val PrimaryVariant = Color(0xFF1D4ED8)

// ==================== BACKGROUND & SURFACE ====================
/**
 * Abu-abu sangat muda/cool grey untuk background layar
 * Agar mata tidak cepat lelah
 */
val BackgroundLight = Color(0xFFF8F9FA)

/**
 * Putih murni untuk Kartu Tugas dan Dialog
 */
val SurfaceWhite = Color(0xFFFFFFFF)

// ==================== TEXT COLORS ====================
/**
 * Slate Dark - Hitam kebiruan, lebih elegan dari hitam pekat
 * Untuk judul dan teks utama
 */
val TextPrimary = Color(0xFF1E293B)

/**
 * Slate Grey - Untuk tanggal/subtitle/secondary text
 */
val TextSecondary = Color(0xFF64748B)

// ==================== SEMANTIC COLORS ====================
/**
 * Emerald - Untuk checklist selesai/success state
 */
val SuccessGreen = Color(0xFF10B981)

/**
 * Soft Red - Untuk deadline lewat/hapus/error state
 */
val ErrorRed = Color(0xFFEF4444)

/**
 * Amber - Khusus ikon Pin
 */
val PinGold = Color(0xFFF59E0B)

// ==================== ADDITIONAL UTILITY COLORS ====================
/**
 * Warning Orange - Untuk peringatan
 */
val WarningOrange = Color(0xFFF97316)

/**
 * Divider/Border color - Abu-abu muda untuk garis pemisah
 */
val DividerGrey = Color(0xFFE2E8F0)

/**
 * Disabled/Muted color
 */
val DisabledGrey = Color(0xFF94A3B8)

// ==================== LEGACY ALIASES (untuk kompatibilitas) ====================
// Alias untuk menjaga kompatibilitas dengan kode yang sudah ada
val OnBackgroundWhite = TextPrimary
val OnSurfaceVariant = TextSecondary
val SurfaceCard = SurfaceWhite
val BackgroundDark = BackgroundLight // Renamed but aliased for compatibility
val TaskCardDark = SurfaceWhite // Task card now uses white surface