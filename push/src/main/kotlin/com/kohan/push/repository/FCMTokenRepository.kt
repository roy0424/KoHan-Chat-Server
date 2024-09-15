package com.kohan.push.repository

import com.kohan.push.collection.FCMTokenCollection
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.LocalDateTime

interface FCMTokenRepository : MongoRepository<FCMTokenCollection, ObjectId> {
    fun findByUserId(userId: ObjectId): FCMTokenCollection?

    fun save(fcmTokenCollection: FCMTokenCollection): FCMTokenCollection

    fun findByTokensAccessedAtBefore(expiryDate: LocalDateTime): List<FCMTokenCollection>
}
