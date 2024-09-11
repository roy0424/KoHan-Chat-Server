package com.kohan.shared.armeria.client.grpc.authentication

import com.google.common.util.concurrent.ListenableFuture
import com.kohan.shared.armeria.authentication.v1.Authentication.UserDto
import com.kohan.shared.armeria.authentication.v1.Authentication.UserToken
import com.kohan.shared.armeria.authentication.v1.AuthenticationServiceGrpc.AuthenticationServiceFutureStub
import com.linecorp.armeria.client.grpc.GrpcClients
import io.github.cdimascio.dotenv.dotenv

object AuthenticationClient {
    private val authenticationServiceFutureStub: AuthenticationServiceFutureStub = GrpcClients.newClient(
        dotenv()["AUTHENTICATION_GRPC_ADDRESS"],
        AuthenticationServiceFutureStub::class.java
    )

    fun getUserInfo(token: String): ListenableFuture<UserDto> {
        val userToken = UserToken.newBuilder()
            .setToken(token)
            .build()

        return authenticationServiceFutureStub.authenticateUser(userToken)
    }
}