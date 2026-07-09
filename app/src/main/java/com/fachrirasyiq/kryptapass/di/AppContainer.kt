package com.fachrirasyiq.kryptapass.di

import android.content.Context
import com.fachrirasyiq.kryptapass.repository.PasswordRepository
import com.fachrirasyiq.kryptapass.security.CryptoManager
import com.fachrirasyiq.kryptapass.security.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Wadah Dependency Injection untuk menyediakan dependensi di tingkat aplikasi.
 */
interface AppContainer {
    val passwordRepository: PasswordRepository
    val cryptoManager: CryptoManager
    val sessionManager: SessionManager
    val auth: FirebaseAuth
    val networkMonitor: com.fachrirasyiq.kryptapass.security.NetworkMonitor
}

/**
 * Implementasi AppContainer yang menginisialisasi dependensi secara lazy.
 */
class DefaultAppContainer(private val context: Context) : AppContainer {

    override val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    // Mengelola proses enkripsi/dekripsi data menggunakan AES-256-GCM
    override val cryptoManager: CryptoManager by lazy {
        CryptoManager()
    }

    // Mengelola kunci sesi aktif di dalam memori (dihapus otomatis saat aplikasi masuk ke background)
    override val sessionManager: SessionManager by lazy {
        SessionManager()
    }

    override val networkMonitor: com.fachrirasyiq.kryptapass.security.NetworkMonitor by lazy {
        com.fachrirasyiq.kryptapass.security.NetworkMonitor(context)
    }

    override val passwordRepository: PasswordRepository by lazy {
        PasswordRepository(
            firestore = firestore,
            auth = auth
        )
    }
}
