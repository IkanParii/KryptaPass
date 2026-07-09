package com.fachrirasyiq.kryptapass.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fachrirasyiq.kryptapass.repository.PasswordRepository
import com.fachrirasyiq.kryptapass.security.CryptoManager
import com.fachrirasyiq.kryptapass.security.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * State UI untuk proses autentikasi (login/register).
 */
data class AuthUiState(
    val emailInput: String = "",
    val passwordInput: String = "",
    val masterPasswordInput: String = "",
    val isRegisterMode: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

/**
 * Intent untuk aksi autentikasi pengguna.
 */
sealed interface AuthIntent {
    data class OnEmailChange(val email: String) : AuthIntent
    data class OnPasswordChange(val pass: String) : AuthIntent
    data class OnMasterPasswordChange(val pass: String) : AuthIntent
    object ToggleMode : AuthIntent
    object Submit : AuthIntent
    object SignOut : AuthIntent
    object ClearError : AuthIntent
}

/**
 * ViewModel untuk menangani alur pendaftaran, masuk log (login), dan keluar log (logout).
 */
class AuthViewModel(
    private val firebaseAuth: FirebaseAuth,
    private val cryptoManager: CryptoManager,
    private val sessionManager: SessionManager,
    private val repository: PasswordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(isAuthenticated = firebaseAuth.currentUser != null && sessionManager.isSessionActive())
    )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.isSessionActiveFlow.collect { isActive ->
                _uiState.update { it.copy(isAuthenticated = firebaseAuth.currentUser != null && isActive) }
            }
        }
    }

    fun handleIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.OnEmailChange ->
                _uiState.update { it.copy(emailInput = intent.email.trim(), error = null) }
            is AuthIntent.OnPasswordChange ->
                _uiState.update { it.copy(passwordInput = intent.pass, error = null) }
            is AuthIntent.OnMasterPasswordChange ->
                _uiState.update { it.copy(masterPasswordInput = intent.pass, error = null) }
            is AuthIntent.ToggleMode ->
                _uiState.update { it.copy(isRegisterMode = !it.isRegisterMode, error = null) }
            is AuthIntent.Submit -> submitAuth()
            is AuthIntent.SignOut -> {
                firebaseAuth.signOut()
                sessionManager.clearSession()
                _uiState.update { AuthUiState() }
            }
            is AuthIntent.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    private fun submitAuth() {
        val state = _uiState.value
        val sanitizedEmail = state.emailInput.trim()

        if (sanitizedEmail.isBlank()) {
            _uiState.update { it.copy(error = "Email tidak boleh kosong") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(sanitizedEmail).matches()) {
            _uiState.update { it.copy(error = "Format email tidak valid") }
            return
        }
        if (state.passwordInput.isBlank()) {
            _uiState.update { it.copy(error = "Kata sandi tidak boleh kosong") }
            return
        }
        if (state.passwordInput.length < 6) {
            _uiState.update { it.copy(error = "Kata sandi minimal 6 karakter") }
            return
        }
        if (state.masterPasswordInput.isBlank()) {
            _uiState.update { it.copy(error = "Master password tidak boleh kosong") }
            return
        }
        if (state.masterPasswordInput.length < 8) {
            _uiState.update { it.copy(error = "Master password minimal 8 karakter") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        if (state.isRegisterMode) {
            register(sanitizedEmail, state.passwordInput, state.masterPasswordInput)
        } else {
            login(sanitizedEmail, state.passwordInput, state.masterPasswordInput)
        }
    }

    /**
     * Menangani pendaftaran pengguna baru: membuat akun di Firebase,
     * menyimpan salt unik untuk enkripsi di Firestore, lalu menghasilkan kunci utama.
     */
    private fun register(email: String, password: String, masterPassword: String) {
        viewModelScope.launch {
            try {
                val result = firebaseAuth
                    .createUserWithEmailAndPassword(email, password)
                    .await()

                val uid = result.user?.uid
                    ?: throw Exception("Gagal mendapatkan ID pengguna")

                val salt = cryptoManager.generateSalt()
                repository.saveSalt(uid, salt)

                val secretKey = cryptoManager.deriveKey(masterPassword, salt)
                sessionManager.setSessionKey(secretKey)
                sessionManager.setUserPassword(password)

                repository.logAudit("Registrasi akun berhasil")

                _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }

            } catch (e: FirebaseAuthException) {
                _uiState.update { it.copy(isLoading = false, error = translateFirebaseError(e.errorCode)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Registrasi gagal") }
            }
        }
    }

    /**
     * Menangani proses login: memverifikasi kredensial di Firebase,
     * mengambil salt yang tersimpan, lalu merekonstruksi kunci enkripsi pengguna.
     */
    private fun login(email: String, password: String, masterPassword: String) {
        viewModelScope.launch {
            try {
                val result = firebaseAuth
                    .signInWithEmailAndPassword(email, password)
                    .await()

                val uid = result.user?.uid
                    ?: throw Exception("Gagal mendapatkan ID pengguna")

                val salt = repository.getSalt(uid)
                    ?: throw Exception(
                        "Data enkripsi tidak ditemukan untuk akun ini. " +
                        "Kemungkinan akun dibuat dengan versi lama aplikasi."
                    )

                val secretKey = cryptoManager.deriveKey(masterPassword, salt)
                sessionManager.setSessionKey(secretKey)
                sessionManager.setUserPassword(password)

                repository.logAudit("Login berhasil")

                _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }

            } catch (e: FirebaseAuthException) {
                _uiState.update { it.copy(isLoading = false, error = translateFirebaseError(e.errorCode)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Login gagal") }
            }
        }
    }

    /**
     * Menerjemahkan kode error Firebase Auth ke pesan Bahasa Indonesia yang ramah pengguna.
     */
    private fun translateFirebaseError(errorCode: String): String = when (errorCode) {
        "ERROR_INVALID_EMAIL"            -> "Format email tidak valid"
        "ERROR_USER_NOT_FOUND"           -> "Akun dengan email ini tidak ditemukan"
        "ERROR_WRONG_PASSWORD"           -> "Kata sandi salah. Coba lagi"
        "ERROR_INVALID_CREDENTIAL"       -> "Email atau kata sandi salah"
        "ERROR_EMAIL_ALREADY_IN_USE"     -> "Email ini sudah terdaftar. Silakan login"
        "ERROR_WEAK_PASSWORD"            -> "Kata sandi terlalu lemah. Gunakan minimal 6 karakter"
        "ERROR_NETWORK_REQUEST_FAILED"   -> "Tidak ada koneksi internet. Periksa jaringan Anda"
        "ERROR_TOO_MANY_REQUESTS"        -> "Terlalu banyak percobaan. Coba lagi beberapa saat"
        "ERROR_USER_DISABLED"            -> "Akun ini telah dinonaktifkan"
        "ERROR_OPERATION_NOT_ALLOWED"    -> "Metode login ini tidak diizinkan"
        else                             -> "Terjadi kesalahan. Coba lagi ($errorCode)"
    }
}
