package com.kohan.shared.armeria.client.grpc

import com.kohan.proto.rest.v1.UserProfileOuterClass
import com.kohan.proto.rest.v1.UserProfileServiceGrpcKt
import com.linecorp.armeria.client.grpc.GrpcClients
import io.github.cdimascio.dotenv.dotenv

object RestGrpcClient {
    private val port = dotenv()["REST_PORT"]

    private val client =
        GrpcClients.newClient(
            "gproto+http://localhost:$port/grpc/v1/",
            UserProfileServiceGrpcKt.UserProfileServiceCoroutineStub::class.java,
        )

    suspend fun initUserProfile(
        userId: String,
        name: String,
        profileImageFileId: String,
    ) {
        client.initUserProfile(
            UserProfileOuterClass.UserProfile
                .newBuilder()
                .setUserId(userId)
                .setNickname(name)
                .setProfileImageFileId(profileImageFileId)
                .build(),
        )
    }
}
