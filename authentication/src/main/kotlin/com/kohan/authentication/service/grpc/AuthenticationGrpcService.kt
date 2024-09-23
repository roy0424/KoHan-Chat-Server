package com.kohan.authentication.service.grpc

import com.kohan.authentication.exception.code.UserErrorCode
import com.kohan.authentication.repository.UserRepository
import com.kohan.proto.authentication.v1.Authentication
import com.kohan.proto.authentication.v1.AuthenticationServiceGrpcKt.AuthenticationServiceCoroutineImplBase
import com.kohan.shared.armeria.exception.BusinessException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class AuthenticationServiceImpl(
    private val userRepository: UserRepository,
) : AuthenticationServiceCoroutineImplBase() {
    override suspend fun authenticateUser(request: Authentication.UserToken): Authentication.UserDto {
        try {
            val user =
                withContext(Dispatchers.IO) {
                    userRepository.findByTokenInfosToken(request.token)
                } ?: throw UserErrorCode.NOT_FOUND_USER.businessException
            return Authentication.UserDto
                .newBuilder()
                .setUserId(user.id.toString())
                .build()
        } catch (e: BusinessException) {
            return Authentication.UserDto
                .newBuilder()
                .setError(e.message)
                .build()
        }
    }
}