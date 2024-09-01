package com.kohan.authentication.service

import com.kohan.authentication.dto.TokenDto
import com.kohan.authentication.exception.code.UserErrorCode
import com.kohan.authentication.repository.UserRepository
import com.kohan.authentication.util.TokenGenerator
import com.kohan.authentication.util.UserUtil
import com.kohan.authentication.vo.SignIn
import com.kohan.authentication.vo.SignUp
import com.kohan.shared.armeria.converter.request.AccessDeviceRequestConverter
import com.kohan.shared.armeria.converter.request.result.AccessDeviceInfo
import com.kohan.shared.armeria.exception.handler.BusinessExceptionHandler
import com.kohan.shared.spring.exception.handler.ConstraintViolationExceptionHandler
import com.kohan.shared.spring.exception.handler.MismatchedInputExceptionHandler
import com.linecorp.armeria.server.annotation.ExceptionHandler
import com.linecorp.armeria.server.annotation.Post
import com.linecorp.armeria.server.annotation.ProducesJson
import com.linecorp.armeria.server.annotation.RequestConverter
import com.linecorp.armeria.server.annotation.RequestObject
import jakarta.validation.Valid
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

@Service
@Validated
@ExceptionHandler(BusinessExceptionHandler::class)
@ExceptionHandler(ConstraintViolationExceptionHandler::class)
@ExceptionHandler(MismatchedInputExceptionHandler::class)
class UserService(
    private val userRepository: UserRepository,
    private val userUtil: UserUtil,
    private val tokenGenerator: TokenGenerator,
) {
    @Post("/sign-up")
    @ProducesJson
    fun signUp(
        @Valid
        @RequestObject
        signUp: SignUp,
        @RequestConverter(AccessDeviceRequestConverter::class)
        accessDeviceInfo: AccessDeviceInfo,
    ): TokenDto {
        if (userRepository.existsByEmail(signUp.email)) throw UserErrorCode.DUPLICATED_EMAIL.businessException

        val newToken = tokenGenerator.generate(accessDeviceInfo)
        val newUser = userUtil.toUserCollection(signUp, newToken)
        userRepository.save(newUser)

        return newToken.toDto()
    }

    @Post("/sign-in")
    @ProducesJson
    @Transactional
    fun signIn(
        @Valid
        @RequestObject
        signIn: SignIn,
        @RequestConverter(AccessDeviceRequestConverter::class)
        accessDeviceInfo: AccessDeviceInfo,
    ): TokenDto {
        val user = userRepository.findByEmail(signIn.email) ?: throw UserErrorCode.NOT_FOUND_USER.businessException
        if (!userUtil.matches(signIn.password, user.password)) throw UserErrorCode.NOT_FOUND_USER.businessException

        user.tokenInfos.firstOrNull { info -> info.accessDeviceInfo == accessDeviceInfo }?.let {
            user.tokenInfos.removeIf { info -> info == it }
        }

        val newToken = tokenGenerator.generate(accessDeviceInfo)
        user.tokenInfos.add(newToken)
        userRepository.save(user)

        return newToken.toDto()
    }
}
