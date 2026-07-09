package com.fachrirasyiq.kryptapass.security

import kotlin.math.log2
import kotlin.math.pow

/**
 * Menyimpan hasil evaluasi kekuatan password, termasuk nilai entropi, estimasi waktu crack, skor, dan detail pengecekan.
 */
data class PasswordCheckResult(
    val entropy: Double,
    val crackTimeDisplay: String, // Estimasi waktu crack yang mudah dibaca oleh pengguna
    val score: Int,               // Skor kekuatan password dari rentang 1 sampai 3
    val checks: List<PasswordCheckItem>
)

/**
 * Menyimpan status hasil pengecekan untuk kriteria password tertentu.
 */
data class PasswordCheckItem(
    val label: String,
    val passed: Boolean,
    val detail: String? = null
)

/**
 * Kelas pembantu untuk mengevaluasi kekuatan password pengguna berdasarkan kriteria tertentu (panjang, variasi karakter, kemiripan).
 */
class PasswordChecker {

    /**
     * Melakukan evaluasi menyeluruh terhadap password yang dimasukkan.
     * Mengembalikan objek [PasswordCheckResult] yang berisi rincian hasil evaluasi.
     */
    fun check(password: String, username: String?, platformName: String?): PasswordCheckResult {
        if (password.isEmpty()) {
            return PasswordCheckResult(
                entropy = 0.0,
                crackTimeDisplay = "",
                score = 0,
                checks = emptyList()
            )
        }

        val checks = mutableListOf<PasswordCheckItem>()

        // Kriteria 1: Panjang karakter minimal 8
        val lengthPass = password.length >= 8
        checks.add(
            PasswordCheckItem(
                label = "Minimal 8 karakter",
                passed = lengthPass,
                detail = if (!lengthPass) "Hanya ${password.length}/8 karakter" else null
            )
        )

        // Kriteria 2: Mengandung kombinasi variasi karakter (huruf besar/kecil, angka, simbol)
        val hasLower = password.any { it.isLowerCase() }
        val hasUpper = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSymbol = password.any { !it.isLetterOrDigit() }
        val categoryCount = listOf(hasLower, hasUpper, hasDigit, hasSymbol).count { it }
        val categoryPass = categoryCount >= 3
        checks.add(
            PasswordCheckItem(
                label = "Huruf besar & kecil, angka, atau simbol (min. 3 jenis)",
                passed = categoryPass,
                detail = when {
                    !categoryPass && categoryCount < 3 -> "Baru memenuhi $categoryCount dari 3 jenis"
                    else -> null
                }
            )
        )

        // Kriteria 3: Menghindari kemiripan password dengan username atau nama platform
        var similar = false
        if (username != null && username.isNotBlank()) {
            val dist = levenshteinDistance(password, username)
            if (dist <= 5) similar = true
        }
        if (platformName != null && platformName.isNotBlank() && !similar) {
            val dist = levenshteinDistance(password, platformName)
            if (dist <= 5) similar = true
        }
        checks.add(
            PasswordCheckItem(
                label = "Tidak mirip dengan username/nama platform",
                passed = !similar,
                detail = if (similar) "Password terlalu mirip dengan data akun" else null
            )
        )

        val entropy = calculateEntropy(password)
        val crackTime = formatCrackTime(entropy)

        val score = listOf(lengthPass, categoryPass, !similar).count { it }

        return PasswordCheckResult(
            entropy = entropy,
            crackTimeDisplay = crackTime,
            score = if (password.isEmpty()) 0 else maxOf(1, score),
            checks = checks
        )
    }

    /**
     * Menghitung tingkat entropi (keacakan) dari password.
     * Entropi yang lebih tinggi menunjukkan keamanan yang lebih kuat.
     */
    private fun calculateEntropy(password: String): Double {
        if (password.isEmpty()) return 0.0
        var charsetSize = 0
        if (password.any { it.isLowerCase() }) charsetSize += 26
        if (password.any { it.isUpperCase() }) charsetSize += 26
        if (password.any { it.isDigit() }) charsetSize += 10
        if (password.any { !it.isLetterOrDigit() }) charsetSize += 33
        if (charsetSize == 0) charsetSize = 1
        return log2(charsetSize.toDouble()) * password.length
    }

    // Mengkonversi estimasi waktu tebak (crack) ke format string yang mudah dipahami
    private fun formatCrackTime(entropy: Double): String {
        val seconds = 2.0.pow(entropy) / 10_000_000_000.0
        return when {
            seconds < 1.0 -> "Seketika"
            seconds < 60.0 -> "${seconds.toInt()} detik"
            seconds < 3600.0 -> "${(seconds / 60).toInt()} menit"
            seconds < 86400.0 -> "${(seconds / 3600).toInt()} jam"
            seconds < 2_592_000.0 -> "${(seconds / 86400).toInt()} hari"
            seconds < 31_536_000.0 -> "${(seconds / 2_592_000).toInt()} bulan"
            seconds < 3_153_600_000.0 -> "${(seconds / 31_536_000).toInt()} tahun"
            else -> "Berabad-abad"
        }
    }

    // Algoritma Levenshtein Distance untuk menghitung tingkat kesamaan antar teks
    private fun levenshteinDistance(a: CharSequence, b: CharSequence): Int {
        val costs = IntArray(b.length + 1)
        for (j in 0..b.length) costs[j] = j
        for (i in 1..a.length) {
            costs[0] = i
            var previous = i - 1
            for (j in 1..b.length) {
                val current = costs[j]
                costs[j] = minOf(
                    1 + costs[j],
                    1 + costs[j - 1],
                    previous + if (a[i - 1] == b[j - 1]) 0 else 1
                )
                previous = current
            }
        }
        return costs[b.length]
    }
}
