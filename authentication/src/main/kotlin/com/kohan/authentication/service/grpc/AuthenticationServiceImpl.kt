package com.kohan.authentication.service.grpc

import com.kohan.authentication.repository.UserRepository
import com.kohan.authentication.util.UserUtil
import com.kohan.shared.armeria.authentication.v1.Authentication
import com.kohan.shared.armeria.authentication.v1.AuthenticationServiceGrpcKt.AuthenticationServiceCoroutineImplBase
import org.springframework.stereotype.Service

@Service
class AuthenticationServiceImpl(
    private val userRepository: UserRepository,
    private val userUtil: UserUtil,
) : AuthenticationServiceCoroutineImplBase() {
    override suspend fun authenticateUser(request: Authentication.UserToken): Authentication.UserDto {
        return super.authenticateUser(request)
    }
}
