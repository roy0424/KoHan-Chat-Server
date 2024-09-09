package com.kohan.authentication.service.annotation

import com.kohan.authentication.collection.UserCollection
import com.kohan.authentication.collection.item.TokenInfo
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
import org.springframework.dao.DuplicateKeyException
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

        val newUser = userUtil.toUserCollection(signUp)
        val (_, newToken) = saveUserWithNewToken(newUser, accessDeviceInfo)

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

        removeOldToken(user, accessDeviceInfo)
        val (_, newToken) = saveUserWithNewToken(user, accessDeviceInfo)

        return newToken.toDto()
    }

    protected fun saveUserWithNewToken(
        newUser: UserCollection,
        accessDeviceInfo: AccessDeviceInfo,
    ): Pair<UserCollection, TokenInfo> {
        while (true) {
            try {
                val newToken = tokenGenerator.generate(accessDeviceInfo)
                newUser.tokenInfos.add(newToken)

                return Pair(userRepository.save(newUser), newToken)
            } catch (e: DuplicateKeyException) {
                removeOldToken(newUser, accessDeviceInfo)
                continue
            }
        }
    }

    private fun removeOldToken(
        user: UserCollection,
        accessDeviceInfo: AccessDeviceInfo,
    ) {
        user.tokenInfos.firstOrNull { info -> info.accessDeviceInfo == accessDeviceInfo }?.let {
            user.tokenInfos.removeIf { info -> info == it }
        }
    }
}
