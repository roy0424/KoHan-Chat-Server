package com.kohan.authentication.util

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PasswordUtilTest {
    private val passwordUtil = PasswordUtil(16, 32, 4, 65536, 1)

    @Test
    fun hash() {
        val hashed1 = passwordUtil.hash("password")
        val hashed2 = passwordUtil.hash("password")

        assertNotEquals(hashed1, hashed2)
    }

    @Test
    fun matches() {
        val hashed = passwordUtil.hash("password")
        val password = "password"

        assertEquals(true, passwordUtil.matches(password, hashed))
    }
}
