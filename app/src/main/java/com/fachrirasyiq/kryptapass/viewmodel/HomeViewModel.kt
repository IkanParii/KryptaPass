package com.fachrirasyiq.kryptapass.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fachrirasyiq.kryptapass.model.AccountModel
import com.fachrirasyiq.kryptapass.repository.PasswordRepository
import com.fachrirasyiq.kryptapass.security.CryptoManager
import com.fachrirasyiq.kryptapass.security.SessionManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * State UI untuk layar utama (dashboard).
 */
data class HomeUiState(
    val accounts: List<AccountModel> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val decryptionError: String? = null
)

/**
 * ViewModel untuk mengelola daftar akun/password dan melakukan pencarian.
 */
class HomeViewModel(
    private val repository: PasswordRepository,
    private val sessionManager: SessionManager,
    private val firebaseAuth: FirebaseAuth,
    private val cryptoManager: CryptoManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _refreshTrigger = MutableStateFlow(0)

    /**
     * Menggabungkan aliran data akun real-time dari Firestore dengan query pencarian pengguna.
     */
    val uiState: StateFlow<HomeUiState> = combine(
        repository.getAllAccounts(),
        _searchQuery,
        _refreshTrigger
    ) { accounts, query, _ ->
        val filtered = if (query.isBlank()) {
            accounts
        } else {
            accounts.filter {
                it.platformName.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                it.username.contains(query, ignoreCase = true)
            }
        }

        var decError: String? = null
        val sessionKey = sessionManager.getSessionKey()
        if (sessionKey != null && accounts.isNotEmpty()) {
            try {
                // Mencoba mendekripsi akun pertama untuk memvalidasi apakah master password yang diinput benar
                val firstAccount = accounts.firstOrNull { it.passwordCipher.isNotBlank() }
                if (firstAccount != null) {
                    cryptoManager.decrypt(firstAccount.passwordCipher, sessionKey)
                }
            } catch (e: Exception) {
                decError = "Gagal mendekripsi kata sandi Anda. Master password yang dimasukkan mungkin salah."
            }
        }

        HomeUiState(
            accounts = filtered,
            searchQuery = query,
            isLoading = false,
            decryptionError = decError
        )
    }
    .catch { e ->
        val message = when {
            e.message?.contains("PERMISSION_DENIED") == true ->
                "Akses ditolak. Coba logout dan login kembali"
            e.message?.contains("UNAVAILABLE") == true ->
                "Tidak ada koneksi internet"
            e.message?.contains("UNAUTHENTICATED") == true ->
                "Sesi berakhir. Silakan login kembali"
            else -> "Gagal memuat data: ${e.message}"
        }
        emit(HomeUiState(isLoading = false, error = message))
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun logout() {
        sessionManager.clearSession()
        firebaseAuth.signOut()
    }

    fun verifyAndRestoreMasterPassword(password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val uid = firebaseAuth.currentUser?.uid ?: throw Exception("User tidak masuk")
                val salt = repository.getSalt(uid) ?: throw Exception("Salt tidak ditemukan")
                val newKey = cryptoManager.deriveKey(password, salt)
                
                val accounts = repository.getAllAccounts().first()
                val firstAccount = accounts.firstOrNull { it.passwordCipher.isNotBlank() }
                if (firstAccount != null) {
                    cryptoManager.decrypt(firstAccount.passwordCipher, newKey)
                }
                
                sessionManager.setSessionKey(newKey)
                _refreshTrigger.value += 1
                onSuccess()
            } catch (e: Exception) {
                onFailure("Master password salah. Silakan coba lagi.")
            }
        }
    }
}
