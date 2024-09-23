package com.kohan.shared.armeria.client.grpc

import com.kohan.proto.authentication.v1.Authentication
import com.kohan.proto.authentication.v1.AuthenticationServiceGrpcKt
import com.linecorp.armeria.client.grpc.GrpcClients
import io.github.cdimascio.dotenv.dotenv

object AuthenticationGrpcClient {
    val port = dotenv()["AUTHENTICATION_PORT"]

    private val client = GrpcClients.newClient(
        "gproto+http://localhost:$port/grpc/v1/",
        AuthenticationServiceGrpcKt.AuthenticationServiceCoroutineStub::class.java,
    )

    suspend fun authenticateUser(token: String): String {
        return client.authenticateUser(
            Authentication.UserToken.newBuilder()
                .setToken(token)
                .build()
        ).userId
    }
}