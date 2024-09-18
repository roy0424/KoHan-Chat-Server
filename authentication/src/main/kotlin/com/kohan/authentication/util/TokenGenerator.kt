package com.kohan.authentication.util

import com.kohan.shared.collection.user.item.AccessDeviceInfo
import com.kohan.shared.collection.user.item.TokenInfo
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
    fun generate(accessDeviceInfo: AccessDeviceInfo): TokenInfo {
        val random = SecureRandom()
        val randomArray = ByteArray(length)
        random.nextBytes(randomArray)

        val issuanceDate = LocalDateTime.now()
        return TokenInfo(
            token = Base64.getEncoder().encodeToString(randomArray),
            accessDeviceInfo = accessDeviceInfo,
            expirationMillis = expiration,
            issuanceDate = issuanceDate,
        )
    }
}
