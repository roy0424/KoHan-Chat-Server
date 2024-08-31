package com.kohan.authentication.util

import com.kohan.authentication.collection.item.TokenInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.Base64

@Component
class TokenGenerator(
    @Value("\${kohan.authentication.token.length}") private val length: Int,
    @Value("\${kohan.authentication.token.expiration}") private val expiration: Long,
) {
    fun generate(): TokenInfo {
        val random = SecureRandom()
        val randomArray = ByteArray(length)
        random.nextBytes(randomArray)
        return TokenInfo(
            Base64.getEncoder().encodeToString(randomArray),
            LocalDateTime.now().plus(expiration, java.time.temporal.ChronoUnit.MILLIS),
        )
    }
}
