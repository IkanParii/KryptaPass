package com.fachrirasyiq.kryptapass

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.fachrirasyiq.kryptapass.di.AppContainer
import com.fachrirasyiq.kryptapass.di.DefaultAppContainer

/**
 * Kelas Application dasar untuk aplikasi KryptaPass.
 * Kelas ini berfungsi sebagai titik masuk utama untuk inisialisasi tingkat aplikasi
 * dan mengelola kontainer dependensi global.
 */
class KryptaPassApp : Application(), DefaultLifecycleObserver {
    
    /**
     * Kontainer dependensi tingkat aplikasi.
     * Menyediakan akses terpusat ke dependensi singleton.
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super<Application>.onCreate()
        container = DefaultAppContainer(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        // Kapanpun aplikasi kembali ke foreground, cek apakah timeout (300 detik = 5 menit)
        container.sessionManager.onAppForegrounded(300_000L)
    }

    override fun onStop(owner: LifecycleOwner) {
        // Kapanpun seluruh aplikasi masuk ke background
        container.sessionManager.onAppBackgrounded()
    }
}
