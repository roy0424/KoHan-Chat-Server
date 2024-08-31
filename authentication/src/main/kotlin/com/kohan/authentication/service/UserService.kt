package com.kohan.authentication.service

import com.kohan.authentication.collection.UserCollection
import com.kohan.authentication.dto.TokenDto
import com.kohan.authentication.exception.code.UserErrorCode
import com.kohan.authentication.repository.UserRepository
import com.kohan.authentication.util.TokenGenerator
import com.kohan.authentication.util.UserUtil
import com.kohan.authentication.vo.SignIn
import com.kohan.authentication.vo.SignUp
import com.kohan.shared.armeria.exception.handler.BusinessExceptionHandler
import com.kohan.shared.spring.exception.handler.ConstraintViolationExceptionHandler
import com.linecorp.armeria.server.annotation.ExceptionHandler
import com.linecorp.armeria.server.annotation.Post
import com.linecorp.armeria.server.annotation.RequestObject
import jakarta.validation.Valid
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import java.time.LocalDateTime

@Service
@Validated
@ExceptionHandler(BusinessExceptionHandler::class)
@ExceptionHandler(ConstraintViolationExceptionHandler::class)
class UserService(
    private val userRepository: UserRepository,
    private val userUtil: UserUtil,
    private val tokenGenerator: TokenGenerator,
) {
    @Post("/sign-up")
    fun signUp(
        @Valid signUp: SignUp,
    ) {
        if (userRepository.existsByEmail(signUp.email)) {
            throw UserErrorCode.DUPLICATED_EMAIL.businessException
        }
        saveUserUntilUniqueToken(userUtil.toUserCollection(signUp))
    }

    @Post("/sign-in")
    @Transactional
    fun signIn(
        @Valid @RequestObject signIn: SignIn,
    ): TokenDto {
        val user = userRepository.findByEmail(signIn.email) ?: throw UserErrorCode.NOT_FOUND_USER.businessException
        if (!userUtil.matches(signIn.password, user.password)) {
            throw UserErrorCode.NOT_FOUND_USER.businessException
        }

        return TokenDto("", LocalDateTime.now())
    }

    private fun saveUserUntilUniqueToken(userCollection: UserCollection): UserCollection {
        while (true) {
            try {
                return userRepository.save(userCollection)
            } catch (_: Exception) {
//                userCollection.tokenInfo = tokenGenerator.generate()
            }
        }
    }
}
