package com.kohan.push.service.grpc

import com.google.protobuf.Empty
import com.kohan.push.collection.FCMTokenCollection
import com.kohan.push.collection.item.FCMTokenInfo
import com.kohan.push.repository.FCMTokenRepository
import com.kohan.shared.armeria.push.v1.FCMTokenServiceGrpcKt
import com.kohan.shared.armeria.push.v1.FcmToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class FCMTokenGrpcService(
    private val fcmTokenRepository: FCMTokenRepository,
) : FCMTokenServiceGrpcKt.FCMTokenServiceCoroutineImplBase() {
    override suspend fun registerFCMToken(request: FcmToken.RegisterFCMToken): Empty {
        val fcmTokenCollection =
            withContext(Dispatchers.IO) {
                fcmTokenRepository.findByUserId(ObjectId(request.userId))
            }?.let { collection ->
                collection.tokens
                    .stream()
                    .filter { it.token == request.token }
                    .findFirst()
                    .ifPresentOrElse(
                        { it.accessedAt = LocalDateTime.parse(request.accessedAt) },
                        { collection.tokens.add(FCMTokenInfo.to(request)) },
                    )
                collection
            } ?: FCMTokenCollection.to(request)

        fcmTokenRepository.save(fcmTokenCollection)

        return Empty.getDefaultInstance()
    }

    override suspend fun unregisterFCMToken(request: FcmToken.UnRegisterFCMToken): Empty {
        val fcmTokenCollection =
            withContext(Dispatchers.IO) {
                fcmTokenRepository.findByUserId(ObjectId(request.userId))
            }?.let { collection ->
                collection.tokens.removeIf { it.token == request.token }
                collection
            }
                ?: throw IllegalArgumentException("FCM token not found")

        fcmTokenRepository.save(fcmTokenCollection)

        return Empty.getDefaultInstance()
    }
}
