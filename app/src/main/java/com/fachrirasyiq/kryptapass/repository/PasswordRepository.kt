package com.fachrirasyiq.kryptapass.repository

import android.util.Base64
import com.fachrirasyiq.kryptapass.model.AccountModel
import com.fachrirasyiq.kryptapass.model.AuditModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * PasswordRepository — sumber kebenaran tunggal (single source of truth) untuk data password.
 * Sepenuhnya didukung oleh Firebase Firestore. Tidak ada database lokal.
 *
 * Struktur Firestore:
 *  users/{uid}/accounts/{accountId}   → data akun pengguna
 *  users/{uid}/profile                → salt enkripsi (disimpan sekali saat registrasi)
 *  users/{uid}/audit/{logId}          → catatan audit
 */
class PasswordRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId get() = auth.currentUser?.uid
        ?: throw IllegalStateException("User tidak terautentikasi")

    private fun accountsRef(uid: String) =
        firestore.collection("users").document(uid).collection("accounts")

    private fun profileRef(uid: String) =
        firestore.collection("users").document(uid)

    private fun auditRef(uid: String) =
        firestore.collection("users").document(uid).collection("audit")



    /**
     * Menyimpan salt (dalam format Base64) ke profil pengguna di Firestore.
     * Dipanggil tepat satu kali selama proses registrasi awal.
     */
    suspend fun saveSalt(uid: String, salt: ByteArray) {
        val saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        profileRef(uid).set(mapOf("salt" to saltBase64), com.google.firebase.firestore.SetOptions.merge()).await()
    }

    /**
     * Mengambil salt dari Firestore berdasarkan ID pengguna yang diberikan.
     * Dipanggil saat login untuk melakukan proses key derivation.
     * Mengembalikan nilai null jika profil tidak ditemukan (pengguna baru atau data korup).
     */
    suspend fun getSalt(uid: String): ByteArray? {
        val doc = profileRef(uid).get().await()
        val saltBase64 = doc.getString("salt") ?: return null
        return Base64.decode(saltBase64, Base64.NO_WRAP)
    }



    /**
     * Mengambil semua akun secara real-time menggunakan listener snapshot Firestore.
     * Akan memperbarui UI secara otomatis saat data Firestore berubah (bahkan dari perangkat lain).
     */
    fun getAllAccounts(): Flow<List<AccountModel>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            close(Exception("User tidak terautentikasi"))
            return@callbackFlow
        }

        val listener = accountsRef(uid)
            .orderBy("platformName")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val accounts = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { AccountModel.fromMap(it) }
                } ?: emptyList()
                trySend(accounts)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Mengambil satu entri akun berdasarkan ID-nya.
     */
    suspend fun getAccountById(id: String): AccountModel? {
        val doc = accountsRef(userId).document(id).get().await()
        return doc.data?.let { AccountModel.fromMap(it) }
    }

    /**
     * Menyimpan (insert atau update) akun ke Firestore.
     */
    suspend fun insertAccount(account: AccountModel) {
        accountsRef(userId).document(account.id).set(account.toMap()).await()
    }

    /**
     * Menghapus sebuah akun dari Firestore berdasarkan ID-nya.
     */
    suspend fun deleteAccount(id: String) {
        accountsRef(userId).document(id).delete().await()
    }



    /**
     * Mencatat satu log aktivitas ke koleksi audit di Firestore.
     * Gagal secara diam-diam (tidak melemparkan pengecualian) agar tidak mengganggu alur utama aplikasi.
     */
    suspend fun logAudit(action: String) {
        try {
            val uid = auth.currentUser?.uid ?: return
            val log = AuditModel(
                id = UUID.randomUUID().toString(),
                action = action,
                timestamp = System.currentTimeMillis()
            )
            auditRef(uid).document(log.id).set(log.toMap()).await()
        } catch (_: Exception) {
        }
    }
}
