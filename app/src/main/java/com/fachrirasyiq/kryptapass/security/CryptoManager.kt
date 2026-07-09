package com.fachrirasyiq.kryptapass.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
/**
 * Mengelola semua operasi kriptografi seperti pembuatan salt, hashing master password,
 * derivasi kunci, serta enkripsi/dekripsi data menggunakan algoritma AES-GCM.
 */
class CryptoManager {

    companion object {
        private const val ALGORITHM = "AES/GCM/NoPadding"
        private const val KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val TAG_LENGTH_BIT = 128
        private const val IV_LENGTH_BYTE = 12
        private const val SALT_LENGTH_BYTE = 16
        private const val ITERATION_COUNT = 65536
        private const val KEY_LENGTH = 256
    }

    /**
     * Membuat salt acak sepanjang 16 byte untuk memperkuat hashing password.
     */
    fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH_BYTE)
        SecureRandom().nextBytes(salt)
        return salt
    }

    /**
     * Menghasilkan hash dari master password menggunakan algoritma PBKDF2.
     * Menggabungkan password dan salt untuk mencegah serangan brute-force.
     */
    fun hashMasterPassword(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    /**
     * Menurunkan SecretKey dari master password dan salt untuk digunakan pada enkripsi AES.
     */
    fun deriveKey(password: String, salt: ByteArray): SecretKey {
        val keyBytes = hashMasterPassword(password, salt)
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Mengenkripsi teks biasa menjadi teks terenkripsi menggunakan SecretKey dengan AES-GCM.
     * Hasil kembalian berupa string terenkripsi dengan format Base64 yang menggabungkan IV dan payload terenkripsi.
     */
    fun encrypt(plainText: String, key: SecretKey): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        val iv = ByteArray(IV_LENGTH_BYTE)
        SecureRandom().nextBytes(iv)
        val parameterSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec)

        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        
        val combined = ByteArray(iv.size + cipherText.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
        
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Mendekripsi teks terenkripsi Base64 kembali menjadi teks biasa menggunakan SecretKey.
     */
    fun decrypt(encryptedBase64: String, key: SecretKey): String {
        val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
        
        val iv = ByteArray(IV_LENGTH_BYTE)
        System.arraycopy(combined, 0, iv, 0, iv.size)
        
        val cipherText = ByteArray(combined.size - iv.size)
        System.arraycopy(combined, iv.size, cipherText, 0, cipherText.size)
        
        val cipher = Cipher.getInstance(ALGORITHM)
        val parameterSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec)
        
        val plainTextBytes = cipher.doFinal(cipherText)
        return String(plainTextBytes, Charsets.UTF_8)
    }
}
