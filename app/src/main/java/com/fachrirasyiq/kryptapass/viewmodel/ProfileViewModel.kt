package com.fachrirasyiq.kryptapass.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fachrirasyiq.kryptapass.repository.PasswordRepository
import com.fachrirasyiq.kryptapass.security.SessionManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * State UI untuk layar profil.
 */
data class ProfileUiState(
    val email: String = "",
    val password: String = "",
    val accountCount: Int = 0,
    val isPasswordVisible: Boolean = false,
    val isLoadingCount: Boolean = true
)

/**
 * ViewModel untuk mengelola data profil pengguna, termasuk menghitung jumlah akun tersimpan.
 */
class ProfileViewModel(
    private val repository: PasswordRepository,
    private val sessionManager: SessionManager,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Ambil email dari Firebase Auth user saat ini
        val email = firebaseAuth.currentUser?.email ?: ""
        _uiState.update { it.copy(email = email) }

        // Observe jumlah akun dari Firestore secara real-time
        viewModelScope.launch {
            repository.getAllAccounts()
                .catch { _uiState.update { state -> state.copy(isLoadingCount = false) } }
                .collect { accounts ->
                    _uiState.update { state ->
                        state.copy(accountCount = accounts.size, isLoadingCount = false)
                    }
                }
        }
    }

    /** Dipanggil setelah biometrik sukses untuk memperlihatkan password akun. */
    fun revealPassword() {
        val pass = sessionManager.getUserPassword() ?: "Tidak ditemukan di sesi aktif"
        _uiState.update { it.copy(isPasswordVisible = true, password = pass) }
    }

    /** Sembunyikan kembali password. */
    fun hidePassword() {
        _uiState.update { it.copy(isPasswordVisible = false, password = "") }
    }

    fun logout() {
        sessionManager.clearSession()
        firebaseAuth.signOut()
    }
}
