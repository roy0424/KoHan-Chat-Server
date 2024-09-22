package com.kohan.message.rest.config

import com.kohan.shared.armeria.client.grpc.AuthenticationGrpcClient
import com.linecorp.armeria.common.HttpRequest
import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.common.HttpStatus
import com.linecorp.armeria.server.HttpService
import com.linecorp.armeria.server.ServiceRequestContext
import com.linecorp.armeria.server.SimpleDecoratingHttpService
import io.netty.util.AttributeKey
import kotlinx.coroutines.runBlocking

class TokenValidationService(
    delegate: HttpService
) : SimpleDecoratingHttpService(delegate) {
    override fun serve(ctx: ServiceRequestContext, req: HttpRequest): HttpResponse {
        val authorizationHeader = req.headers()["Authorization"]

        val token = authorizationHeader?.takeIf { it.startsWith("Bearer ") }?.substringAfter("Bearer ")
        return if (token.isNullOrBlank()) {
            HttpResponse.of(HttpStatus(401, "Unauthorized"))
        } else {
            try {
                val userId = runBlocking { AuthenticationGrpcClient.authenticateUser(token) }

                ctx.setAttr(AttributeKey.valueOf("userId"), userId)

                unwrap().serve(ctx, req)
            } catch (e: Exception) {
                HttpResponse.of(HttpStatus(401, "Unauthorized"))
            }
        }
    }
}