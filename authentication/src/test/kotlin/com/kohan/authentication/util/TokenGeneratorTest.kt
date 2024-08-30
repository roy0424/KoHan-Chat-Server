package com.kohan.authentication.util

import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TokenGeneratorTest {
    @Test
    fun checkDuplicateToken() {
        val tokenGenerator = TokenGenerator(32, 1000)
        val tokens = (0..1000).map { tokenGenerator.generate() }.map { it.token }

        assertEquals(tokens.size, tokens.toSet().size)
    }

    @Test
    fun checkExpiration() {
        val tokenGenerator = TokenGenerator(32, 500)
        val token = tokenGenerator.generate()

        Thread.sleep(1000)

        val now = LocalDateTime.now()
        assertTrue(now.isAfter(token.expirationDate))
    }
}
