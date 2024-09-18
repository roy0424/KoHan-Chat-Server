package com.kohan.push.scheduler

import com.kohan.push.firebase.FCMService
import com.kohan.push.firebase.FirebaseConfig
import com.kohan.push.repository.FCMTokenRepository
import com.kohan.shared.collection.fcm.FCMTokenCollection
import com.kohan.shared.collection.fcm.item.FCMTokenInfo
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import java.time.LocalDateTime

@AutoConfigureDataMongo
@SpringBootTest(properties = ["de.flapdoodle.mongodb.embedded.version=5.0.5"])
@EnableAutoConfiguration
@DirtiesContext
class FCMTokenCleanupSchedulerTest(
    @Autowired
    private val fcmTokenCleanupScheduler: FCMTokenCleanupScheduler,
    @Autowired
    private val fcmTokenRepository: FCMTokenRepository,
) {
    @MockBean
    private lateinit var firebaseConfig: FirebaseConfig

    @MockBean
    private lateinit var fcmService: FCMService

    val objectId = ObjectId()

    @BeforeEach
    fun setUp() {
        fcmTokenRepository.save(
            FCMTokenCollection(
                userId = objectId,
                tokens =
                    mutableListOf(
                        FCMTokenInfo(
                            token = "token1",
                            accessedAt = LocalDateTime.now().minusDays(60),
                        ),
                        FCMTokenInfo(
                            token = "token2",
                            accessedAt = LocalDateTime.now().minusDays(30),
                        ),
                        FCMTokenInfo(
                            token = "token3",
                            accessedAt = LocalDateTime.now().minusDays(15),
                        ),
                        FCMTokenInfo(
                            token = "token4",
                            accessedAt = LocalDateTime.now(),
                        ),
                    ),
            ),
        )
    }

    @Test
    fun deleteExpiredTokens() {
        fcmTokenCleanupScheduler.deleteExpiredTokens()

        val collection =
            fcmTokenRepository.findByUserId(objectId)
                ?: fail("FCM token collection not found")
        assertEquals(2, collection.tokens.size)
    }

    @AfterEach
    fun tearDown() {
        fcmTokenRepository.deleteAll()
    }
}
