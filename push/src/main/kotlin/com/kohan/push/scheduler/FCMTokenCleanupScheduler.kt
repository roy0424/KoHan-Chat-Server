package com.kohan.push.scheduler

import com.kohan.push.repository.FCMTokenRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class FCMTokenCleanupScheduler(
    private val fcmTokenRepository: FCMTokenRepository,
    @Value("\${kohan.push.firebase.registration.token.expiration.day}")
    private val expirationDate: Long,
) {
    @Transactional
    fun deleteExpiredTokens() {
        val expiryDate = LocalDateTime.now().minusDays(expirationDate)
        fcmTokenRepository
            .findByTokensAccessedAtBefore(expiryDate)
            .forEach { collection ->
                collection.tokens.removeIf { tokenInfo -> tokenInfo.accessedAt.isBefore(expiryDate) }
                collection.tokens.isEmpty().let { if (it) collection.delete() }
                fcmTokenRepository.save(collection)
            }
    }
}
