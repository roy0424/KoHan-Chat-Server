package com.kohan.authentication.service.grpc

import com.kohan.authentication.exception.code.UserErrorCode
import com.kohan.authentication.repository.UserRepository
import com.kohan.shared.armeria.authentication.v1.Authentication
import com.kohan.shared.armeria.authentication.v1.AuthenticationServiceGrpcKt.AuthenticationServiceCoroutineImplBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class AuthenticationServiceImpl(
    private val userRepository: UserRepository,
) : AuthenticationServiceCoroutineImplBase() {
    override suspend fun authenticateUser(request: Authentication.UserToken): Authentication.UserDto {
        val user =
            withContext(Dispatchers.IO) {
                userRepository.findByTokenInfosToken(request.token)
            }
                ?: throw UserErrorCode.INVALID_TOKEN.businessException
        return Authentication.UserDto.newBuilder()
            .setObjectId(user.id.toString())
            .setEmail(user.email)
            .setNickname(user.nickname)
            .build()
    }
}
