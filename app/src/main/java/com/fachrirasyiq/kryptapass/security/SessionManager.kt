package com.fachrirasyiq.kryptapass.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.crypto.SecretKey

/**
 * Mengelola penyimpanan sementara SecretKey di dalam memori untuk proses enkripsi/dekripsi.
 * Untuk keamanan, kunci ini akan dihapus otomatis ketika aplikasi masuk ke background melampaui batas waktu.
 */
class SessionManager {
    private var currentSecretKey: SecretKey? = null
    private var currentUserPassword: String? = null
    
    private val _isSessionActiveFlow = MutableStateFlow(false)
    val isSessionActiveFlow: StateFlow<Boolean> = _isSessionActiveFlow.asStateFlow()

    // Bernilai true ketika sesi kedaluwarsa akibat timeout saat aplikasi di background
    private val _sessionExpiredFlow = MutableStateFlow(false)
    val sessionExpiredFlow: StateFlow<Boolean> = _sessionExpiredFlow.asStateFlow()

    private var backgroundedTimeMillis: Long = 0L

    /**
     * Menyimpan SecretKey yang berhasil dibuat atau dimuat saat proses login.
     */
    fun setSessionKey(key: SecretKey) {
        currentSecretKey = key
        _isSessionActiveFlow.value = true
    }

    /**
     * Mengambil SecretKey aktif untuk kebutuhan enkripsi atau dekripsi.
     */
    fun getSessionKey(): SecretKey? {
        return currentSecretKey
    }

    /**
     * Menghapus SecretKey dan password user dari memori (saat logout atau sesi kedaluwarsa).
     */
    fun clearSession() {
        currentSecretKey = null
        currentUserPassword = null
        _isSessionActiveFlow.value = false
    }

    /**
     * Menyimpan password Firebase user secara sementara di dalam sesi aktif.
     */
    fun setUserPassword(password: String) {
        currentUserPassword = password
    }

    /**
     * Mengambil password Firebase user dari sesi aktif.
     */
    fun getUserPassword(): String? {
        return currentUserPassword
    }

    /**
     * Memeriksa apakah sesi masih aktif (kunci sesi tersedia di memori).
     */
    fun isSessionActive(): Boolean {
        return currentSecretKey != null
    }

    /**
     * Mencatat waktu saat aplikasi masuk ke background.
     */
    fun onAppBackgrounded() {
        backgroundedTimeMillis = System.currentTimeMillis()
    }

    /**
     * Dipanggil saat aplikasi kembali ke foreground.
     * Jika durasi di background melebihi [timeoutMillis], sesi akan dihapus dan mengembalikan nilai true.
     */
    fun onAppForegrounded(timeoutMillis: Long): Boolean {
        if (backgroundedTimeMillis > 0) {
            val timeInBackground = System.currentTimeMillis() - backgroundedTimeMillis
            if (timeInBackground > timeoutMillis) {
                clearSession()
                backgroundedTimeMillis = 0L
                _sessionExpiredFlow.value = true
                return true
            }
        }
        backgroundedTimeMillis = 0L
        return false
    }

    /**
     * Mereset flag status kedaluwarsa sesi setelah dialog ditolak/ditutup oleh pengguna.
     */
    fun consumeSessionExpired() {
        _sessionExpiredFlow.value = false
    }
}
