package com.fachrirasyiq.kryptapass.model

/**
 * Model data untuk menyimpan informasi akun/password pengguna.
 */
data class AccountModel(
    val id: String = "",
    val platformName: String = "",
    val category: String = "",
    val username: String = "",
    val url: String = "",
    val notes: String = "",
    val passwordCipher: String = "",
    val lastUpdated: Long = 0L
) {
    /**
     * Mengkonversi ke Map untuk disimpan ke Firestore.
     */
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "platformName" to platformName,
        "category" to category,
        "username" to username,
        "url" to url,
        "notes" to notes,
        "passwordCipher" to passwordCipher,
        "lastUpdated" to lastUpdated
    )

    companion object {
        /**
         * Membuat AccountModel dari dokumen Firestore.
         */
        fun fromMap(data: Map<String, Any?>): AccountModel = AccountModel(
            id = data["id"] as? String ?: "",
            platformName = data["platformName"] as? String ?: "",
            category = data["category"] as? String ?: "",
            username = data["username"] as? String ?: "",
            url = data["url"] as? String ?: "",
            notes = data["notes"] as? String ?: "",
            passwordCipher = data["passwordCipher"] as? String ?: "",
            lastUpdated = data["lastUpdated"] as? Long ?: 0L
        )
    }
}
