package com.kohan.authentication.service.annotation

import com.kohan.authentication.dto.TokenDto
import com.kohan.authentication.exception.code.UserErrorCode
import com.kohan.authentication.repository.UserRepository
import com.kohan.authentication.util.TokenGenerator
import com.kohan.authentication.util.UserUtil
import com.kohan.authentication.vo.RegistrationToken
import com.kohan.authentication.vo.SignIn
import com.kohan.authentication.vo.SignUp
import com.kohan.shared.armeria.client.grpc.FileGrpcClient
import com.kohan.shared.armeria.client.grpc.PushGrpcClient
import com.kohan.shared.armeria.client.grpc.RestGrpcClient
import com.kohan.shared.armeria.exception.handler.BusinessExceptionHandler
import com.kohan.shared.collection.user.UserCollection
import com.kohan.shared.collection.user.item.AccessDeviceInfo
import com.kohan.shared.collection.user.item.TokenInfo
import com.kohan.shared.spring.converter.request.AccessDeviceRequestConverter
import com.kohan.shared.spring.exception.handler.ConstraintViolationExceptionHandler
import com.kohan.shared.spring.exception.handler.MismatchedInputExceptionHandler
import com.linecorp.armeria.common.AggregatedHttpRequest
import com.linecorp.armeria.common.MediaTypeNames
import com.linecorp.armeria.server.ServiceRequestContext
import com.linecorp.armeria.server.annotation.Consumes
import com.linecorp.armeria.server.annotation.ExceptionHandler
import com.linecorp.armeria.server.annotation.Header
import com.linecorp.armeria.server.annotation.Post
import com.linecorp.armeria.server.annotation.ProducesJson
import com.linecorp.armeria.server.annotation.RequestConverter
import com.linecorp.armeria.server.annotation.RequestObject
import jakarta.validation.Valid
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import ua_parser.Parser

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
    @Consumes(MediaTypeNames.MULTIPART_FORM_DATA)
    @ProducesJson
    suspend fun signUp(
        @Valid
        signUp: SignUp,
        ctx: ServiceRequestContext,
        @Header("user-agent") userAgent: String,
    ): TokenDto {
        val ip = ctx.remoteAddress().address.hostAddress
        val accessDevice = Parser().parse(userAgent)

        val accessDeviceInfo = AccessDeviceInfo(ip, accessDevice.userAgent.family + accessDevice.userAgent.major, accessDevice.os.family + accessDevice.os.major, accessDevice.device.family)

        if (withContext(Dispatchers.IO) {
                userRepository.existsByEmail(signUp.email)
            }) throw UserErrorCode.DUPLICATED_EMAIL.businessException


        val newUser = userUtil.toUserCollection(signUp)
        val (userCollection, newToken) = saveUserWithNewToken(newUser, accessDeviceInfo)

        val profileImageFileId = FileGrpcClient.uploadProfile(
            signUp.profileImage.file(),
            userCollection._id.toHexString()
        )

        RestGrpcClient.initUserProfile(
            userCollection._id.toHexString(),
            signUp.nickname,
            profileImageFileId
        )

        return TokenDto.from(newToken)
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

        return TokenDto.from(newToken)
    }

    @Post("/register-device")
    @ProducesJson
    suspend fun registerDevice(
        @Valid
        @RequestObject
        registrationToken: RegistrationToken,
    ) {
        val user =
            withContext(Dispatchers.IO) {
                userRepository.findByTokenInfosToken(registrationToken.token)
            } ?: throw UserErrorCode.NOT_FOUND_USER.businessException

        user.tokenInfos.firstOrNull { it.token == registrationToken.token }?.let { tokenInfo ->
            tokenInfo.expirationDate.isBefore(LocalDateTime.now()).takeIf { it }?.let {
                throw UserErrorCode.EXPIRED_TOKEN.businessException
            }
        }

        PushGrpcClient.registerFCMToken(user._id.toHexString(), registrationToken.registrationToken)
    }

    @Post("/sign-out")
    @ProducesJson
    @Transactional
    suspend fun signOut(
        @Valid
        @RequestObject
        registrationToken: RegistrationToken,
    ) {
        val user =
            withContext(Dispatchers.IO) {
                userRepository.findByTokenInfosToken(registrationToken.token)
            } ?: throw UserErrorCode.NOT_FOUND_USER.businessException

        user.removeToken(registrationToken.token)
        withContext(Dispatchers.IO) {
            userRepository.save(user)
        }

        PushGrpcClient.unregisterFCMToken(user._id.toHexString(), registrationToken.registrationToken)
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
