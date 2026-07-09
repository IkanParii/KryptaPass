package com.fachrirasyiq.kryptapass.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.fachrirasyiq.kryptapass.KryptaPassApp

/**
 * Penyedia Factory untuk menginisialisasi semua ViewModel dengan dependensi yang sesuai.
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {


        initializer {
            AuthViewModel(
                firebaseAuth = kryptaPassApplication().container.auth,
                cryptoManager = kryptaPassApplication().container.cryptoManager,
                sessionManager = kryptaPassApplication().container.sessionManager,
                repository = kryptaPassApplication().container.passwordRepository
            )
        }


        initializer {
            HomeViewModel(
                repository = kryptaPassApplication().container.passwordRepository,
                sessionManager = kryptaPassApplication().container.sessionManager,
                firebaseAuth = kryptaPassApplication().container.auth,
                cryptoManager = kryptaPassApplication().container.cryptoManager
            )
        }


        initializer {
            PasswordDetailViewModel(
                repository = kryptaPassApplication().container.passwordRepository,
                cryptoManager = kryptaPassApplication().container.cryptoManager,
                sessionManager = kryptaPassApplication().container.sessionManager
            )
        }


        initializer {
            ProfileViewModel(
                repository = kryptaPassApplication().container.passwordRepository,
                sessionManager = kryptaPassApplication().container.sessionManager,
                firebaseAuth = kryptaPassApplication().container.auth
            )
        }
    }
}

/**
 * Ekstensi untuk mendapatkan instans aplikasi KryptaPassApp dari CreationExtras.
 */
fun CreationExtras.kryptaPassApplication(): KryptaPassApp =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as KryptaPassApp)
