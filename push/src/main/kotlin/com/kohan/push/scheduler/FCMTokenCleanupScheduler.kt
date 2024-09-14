package com.kohan.push.scheduler

import com.kohan.push.repository.FCMTokenRepository
import java.time.LocalDateTime
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class FCMTokenCleanupScheduler(
    private val fcmTokenRepository: FCMTokenRepository,
    @Value("\${kohan.push.firebase.registration.token.expiration.day}")
    private val expirationDate: Long
) {
    fun deleteExpiredTokens() {
        val expiryDate = LocalDateTime.now().minusDays(expirationDate)
        fcmTokenRepository.findByTokensAccessedAtBefore(expiryDate)
            .forEach { collection ->
                collection.tokens.removeIf { tokenInfo -> tokenInfo.accessedAt.isBefore(expiryDate) }
                collection.tokens.isEmpty().let { if (it) collection.delete() }
                fcmTokenRepository.save(collection)
            }
    }

}