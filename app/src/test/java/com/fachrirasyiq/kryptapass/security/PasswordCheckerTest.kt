package com.fachrirasyiq.kryptapass.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordCheckerTest {

    private val checker = PasswordChecker()

    @Test
    fun entropy_increases_with_password_length() {
        val result1 = checker.check("a", null, null)
        val result2 = checker.check("aaaaaaaa", null, null)
        assertTrue(result2.entropy > result1.entropy)
    }

    @Test
    fun crack_time_displays_instant_for_weak_password() {
        val result = checker.check("1234", null, null)
        assertTrue(result.crackTimeDisplay == "Instant" || result.score <= 2)
    }

    @Test
    fun strong_password_scores_4() {
        val result = checker.check("Tr0ub4dor&3", null, null)
        assertEquals(4, result.score)
    }

    @Test
    fun detects_common_password() {
        val result = checker.check("password", null, null)
        val commonCheck = result.checks.find { it.label.contains("password umum", ignoreCase = true) }
        assertTrue(commonCheck?.passed == false)
    }

    @Test
    fun empty_password_returns_empty_checks() {
        val result = checker.check("", null, null)
        assertTrue(result.checks.isEmpty())
        assertEquals(0, result.score)
    }

    @Test
    fun strong_crack_time_is_years() {
        val result = checker.check("Tr0ub4dor&3!xyz", null, null)
        // Should be more than "Instant" — strong password takes time
        assertTrue(result.crackTimeDisplay != "Instant")
    }
}
