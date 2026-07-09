package com.fachrirasyiq.kryptapass.model

/**
 * Model data untuk mencatat aktivitas audit ke Firestore.
 */
data class AuditModel(
    val id: String = "",
    val action: String = "",
    val timestamp: Long = 0L
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "action" to action,
        "timestamp" to timestamp
    )
}
