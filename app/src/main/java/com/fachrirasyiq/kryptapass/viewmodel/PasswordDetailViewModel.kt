package com.fachrirasyiq.kryptapass.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fachrirasyiq.kryptapass.model.AccountModel
import com.fachrirasyiq.kryptapass.repository.PasswordRepository
import com.fachrirasyiq.kryptapass.security.CryptoManager
import com.fachrirasyiq.kryptapass.security.PasswordCheckResult
import com.fachrirasyiq.kryptapass.security.PasswordChecker
import com.fachrirasyiq.kryptapass.security.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * State UI untuk mengelola detail kata sandi akun tertentu.
 */
data class PasswordDetailUiState(
    val accountId: String? = null,
    val platformName: String = "",
    val category: String = "Lainnya",
    val username: String = "",
    val url: String = "",
    val notes: String = "",
    val passwordInput: String = "",
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val saveSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,
    val error: String? = null,
    val checkResult: PasswordCheckResult? = null,
    val hasUnsavedChanges: Boolean = false
)

/**
 * Intent untuk aksi pada detail kata sandi.
 */
sealed interface PasswordDetailIntent {
    data class LoadAccount(val id: String) : PasswordDetailIntent
    data class OnPlatformNameChange(val name: String) : PasswordDetailIntent
    data class OnCategoryChange(val category: String) : PasswordDetailIntent
    data class OnUsernameChange(val username: String) : PasswordDetailIntent
    data class OnUrlChange(val url: String) : PasswordDetailIntent
    data class OnNotesChange(val notes: String) : PasswordDetailIntent
    data class OnPasswordChange(val password: String) : PasswordDetailIntent
    object SaveAccount : PasswordDetailIntent
    object DeleteAccount : PasswordDetailIntent
    object ClearError : PasswordDetailIntent
}

/**
 * ViewModel untuk melihat, menambah, memperbarui, atau menghapus detail kata sandi akun.
 */
class PasswordDetailViewModel(
    private val repository: PasswordRepository,
    private val cryptoManager: CryptoManager,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val passwordChecker = PasswordChecker()

    private val _uiState = MutableStateFlow(PasswordDetailUiState())
    val uiState = _uiState.asStateFlow()

    private var originalState = PasswordDetailUiState()

    private fun updateUnsavedChanges(currentState: PasswordDetailUiState): PasswordDetailUiState {
        val hasChanges = currentState.platformName != originalState.platformName ||
            currentState.category != originalState.category ||
            currentState.username != originalState.username ||
            currentState.url != originalState.url ||
            currentState.notes != originalState.notes ||
            currentState.passwordInput != originalState.passwordInput
        return currentState.copy(hasUnsavedChanges = hasChanges)
    }

    fun handleIntent(intent: PasswordDetailIntent) {
        when (intent) {
            is PasswordDetailIntent.LoadAccount -> loadAccount(intent.id)
            is PasswordDetailIntent.OnPlatformNameChange ->
                _uiState.update { updateUnsavedChanges(it.copy(platformName = intent.name, error = null)) }
            is PasswordDetailIntent.OnCategoryChange ->
                _uiState.update { updateUnsavedChanges(it.copy(category = intent.category, error = null)) }
            is PasswordDetailIntent.OnUsernameChange ->
                _uiState.update { updateUnsavedChanges(it.copy(username = intent.username, error = null)) }
            is PasswordDetailIntent.OnUrlChange ->
                _uiState.update { updateUnsavedChanges(it.copy(url = intent.url, error = null)) }
            is PasswordDetailIntent.OnNotesChange ->
                _uiState.update { updateUnsavedChanges(it.copy(notes = intent.notes, error = null)) }
            is PasswordDetailIntent.OnPasswordChange -> {
                _uiState.update {
                    val result = passwordChecker.check(
                        password = intent.password,
                        username = it.username,
                        platformName = it.platformName
                    )
                    updateUnsavedChanges(it.copy(passwordInput = intent.password, checkResult = result, error = null))
                }
            }
            is PasswordDetailIntent.SaveAccount -> saveAccount()
            is PasswordDetailIntent.DeleteAccount -> deleteAccount()
            is PasswordDetailIntent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    /**
     * Memuat data akun dari Firestore dan mendekripsi password terenkripsinya.
     */
    private fun loadAccount(id: String) {
        viewModelScope.launch {
            try {
                val account = repository.getAccountById(id)
                if (account == null) {
                    _uiState.update { it.copy(error = "Akun tidak ditemukan") }
                    return@launch
                }

                val plainPassword = decryptPassword(account.passwordCipher)

                _uiState.update {
                    val result = passwordChecker.check(
                        password = plainPassword,
                        username = account.username,
                        platformName = account.platformName
                    )
                    val newState = it.copy(
                        accountId = account.id,
                        platformName = account.platformName,
                        category = account.category,
                        username = account.username,
                        url = account.url,
                        notes = account.notes,
                        passwordInput = plainPassword,
                        checkResult = result,
                        error = null,
                        hasUnsavedChanges = false
                    )
                    originalState = newState
                    newState
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Gagal memuat akun: ${e.message}") }
            }
        }
    }

    /**
     * Memvalidasi input pengguna, mengenkripsi password dengan kunci sesi aktif, 
     * lalu menyimpan atau memperbaruinya di Firestore.
     */
    private fun saveAccount() {
        val state = _uiState.value
        if (state.platformName.isBlank()) {
            _uiState.update { it.copy(error = "Nama platform tidak boleh kosong") }
            return
        }
        if (state.passwordInput.isBlank()) {
            _uiState.update { it.copy(error = "Kata sandi tidak boleh kosong") }
            return
        }

        val sessionKey = sessionManager.getSessionKey()
        if (sessionKey == null) {
            _uiState.update { it.copy(error = "Sesi enkripsi telah berakhir. Silakan logout dan login kembali") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val encryptedPassword = cryptoManager.encrypt(state.passwordInput, sessionKey)
                val isNewAccount = state.accountId == null
                val account = AccountModel(
                    id = state.accountId ?: UUID.randomUUID().toString(),
                    platformName = state.platformName.trim(),
                    category = state.category,
                    username = state.username.trim(),
                    url = state.url.trim(),
                    notes = state.notes.trim(),
                    passwordCipher = encryptedPassword,
                    lastUpdated = System.currentTimeMillis()
                )

                repository.insertAccount(account)
                repository.logAudit(
                    if (isNewAccount)
                        "Menambahkan kata sandi untuk ${account.platformName}"
                    else
                        "Memperbarui kata sandi untuk ${account.platformName}"
                )

                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("PERMISSION_DENIED") == true ->
                        "Tidak memiliki izin untuk menyimpan data"
                    e.message?.contains("UNAVAILABLE") == true ->
                        "Tidak ada koneksi internet. Periksa jaringan Anda"
                    e.message?.contains("RESOURCE_EXHAUSTED") == true ->
                        "Batas penggunaan Firebase tercapai. Coba lagi nanti"
                    else -> "Gagal menyimpan: ${e.message}"
                }
                _uiState.update { it.copy(isSaving = false, error = errorMsg) }
            }
        }
    }

    /**
     * Menghapus data akun dari Firestore secara permanen.
     */
    private fun deleteAccount() {
        val state = _uiState.value
        if (state.accountId == null) return

        _uiState.update { it.copy(isDeleting = true, error = null) }

        viewModelScope.launch {
            try {
                repository.deleteAccount(state.accountId)
                repository.logAudit("Menghapus kata sandi untuk ${state.platformName}")
                _uiState.update { it.copy(isDeleting = false, deleteSuccess = true) }
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("PERMISSION_DENIED") == true ->
                        "Tidak memiliki izin untuk menghapus data"
                    e.message?.contains("UNAVAILABLE") == true ->
                        "Tidak ada koneksi internet. Periksa jaringan Anda"
                    else -> "Gagal menghapus: ${e.message}"
                }
                _uiState.update { it.copy(isDeleting = false, error = errorMsg) }
            }
        }
    }

    /**
     * Mendekripsi password terenkripsi dari Firestore menggunakan kunci sesi aktif.
     */
    private fun decryptPassword(cipher: String): String {
        val key = sessionManager.getSessionKey()
            ?: return "[Sesi berakhir — logout dan login kembali]"
        return try {
            cryptoManager.decrypt(cipher, key)
        } catch (e: Exception) {
            "[Gagal mendekripsi — master password mungkin salah]"
        }
    }
}
