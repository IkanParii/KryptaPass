package com.fachrirasyiq.kryptapass.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Mengelola dialog verifikasi biometrik (sidik jari/wajah) 
 * serta fallback menggunakan kredensial perangkat (PIN/Pola/Sandi).
 */
class BiometricPromptManager(
    private val activity: FragmentActivity
) {
    private val promptResults = Channel<BiometricResult>()
    val promptResultsFlow = promptResults.receiveAsFlow()

    /**
     * Menampilkan dialog autentikasi biometrik kepada pengguna.
     * Hasil autentikasi dikirimkan melalui [promptResultsFlow].
     */
    fun showBiometricPrompt(
        title: String,
        description: String
    ) {
        val manager = BiometricManager.from(activity)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setDescription(description)
            .setAllowedAuthenticators(authenticators)
            .build()

        when (manager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                promptResults.trySend(BiometricResult.HardwareUnavailable)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                promptResults.trySend(BiometricResult.FeatureUnavailable)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                promptResults.trySend(BiometricResult.AuthenticationNotSet)
                return
            }
            else -> Unit
        }

        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    promptResults.trySend(BiometricResult.AuthenticationError(errString.toString()))
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    promptResults.trySend(BiometricResult.AuthenticationSuccess)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    promptResults.trySend(BiometricResult.AuthenticationFailed)
                }
            }
        )

        prompt.authenticate(promptInfo)
    }

    /**
     * Representasi hasil dari proses autentikasi biometrik.
     */
    sealed interface BiometricResult {
        object HardwareUnavailable : BiometricResult
        object FeatureUnavailable : BiometricResult
        data class AuthenticationError(val error: String) : BiometricResult
        object AuthenticationFailed : BiometricResult
        object AuthenticationSuccess : BiometricResult
        object AuthenticationNotSet : BiometricResult
    }
}
