package com.kohan.shared.armeria.client.grpc

import com.kohan.proto.push.v1.FCMTokenServiceGrpcKt
import com.kohan.proto.push.v1.FcmToken
import com.linecorp.armeria.client.grpc.GrpcClients
import io.github.cdimascio.dotenv.dotenv
import java.time.LocalDateTime

object PushGrpcClient {
    private val port = dotenv()["PUSH_PORT"]

    private val client = GrpcClients.newClient(
        "gproto+http://localhost:$port/grpc/v1/",
        FCMTokenServiceGrpcKt.FCMTokenServiceCoroutineStub::class.java,
    )

    suspend fun registerFCMToken(userId: String, fcmToken: String) {
        client.registerFCMToken(
            FcmToken.RegisterFCMToken.newBuilder()
                .setFcmTokenInfo(
                    FcmToken.FCMTokenInfo.newBuilder()
                        .setUserId(userId)
                        .setToken(fcmToken)
                        .build()
                )
                .setAccessedAt(LocalDateTime.now().toString())
                .build()
        )
    }

    suspend fun unregisterFCMToken(userId: String, fcmToken: String) {
        client.unregisterFCMToken(
            FcmToken.FCMTokenInfo.newBuilder()
                .setToken(fcmToken)
                .setUserId(userId)
                .build()
        )
    }
}