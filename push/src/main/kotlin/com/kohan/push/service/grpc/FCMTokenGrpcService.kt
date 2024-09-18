package com.kohan.push.service.grpc

import com.google.protobuf.Empty
import com.kohan.proto.push.v1.FCMTokenServiceGrpcKt
import com.kohan.proto.push.v1.FcmToken
import com.kohan.push.repository.FCMTokenRepository
import com.kohan.shared.collection.fcm.FCMTokenCollection
import com.kohan.shared.collection.fcm.item.FCMTokenInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class FCMTokenGrpcService(
    private val fcmTokenRepository: FCMTokenRepository,
) : FCMTokenServiceGrpcKt.FCMTokenServiceCoroutineImplBase() {
    @Transactional
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

    @Transactional
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
