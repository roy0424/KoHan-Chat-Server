package com.kohan.message.rest.service.annotation

import com.kohan.message.rest.dto.UserProfileDto
import com.kohan.message.rest.exception.code.UserErrorCode
import com.kohan.message.rest.repository.user.profile.UserProfileRepository
import com.kohan.message.rest.vo.user.profile.Nickname
import com.kohan.message.rest.vo.user.profile.ProfileImage
import com.kohan.message.rest.vo.user.profile.StatusMessage
import com.kohan.shared.armeria.client.grpc.FileGrpcClient
import com.kohan.shared.armeria.exception.handler.BusinessExceptionHandler
import com.kohan.shared.spring.exception.handler.ConstraintViolationExceptionHandler
import com.kohan.shared.spring.exception.handler.MismatchedInputExceptionHandler
import com.linecorp.armeria.common.MediaTypeNames
import com.linecorp.armeria.server.ServiceRequestContext
import com.linecorp.armeria.server.annotation.Consumes
import com.linecorp.armeria.server.annotation.ExceptionHandler
import com.linecorp.armeria.server.annotation.Get
import com.linecorp.armeria.server.annotation.Post
import com.linecorp.armeria.server.annotation.ProducesJson
import io.netty.util.AttributeKey
import jakarta.validation.Valid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Service
@Validated
@ExceptionHandler(BusinessExceptionHandler::class)
@ExceptionHandler(ConstraintViolationExceptionHandler::class)
@ExceptionHandler(MismatchedInputExceptionHandler::class)
class UserProfileService(
    private val userProfileRepository: UserProfileRepository
) {
    @Get
    @ProducesJson
    fun getUserProfile(ctx: ServiceRequestContext): UserProfileDto {
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))
            ?: throw UserErrorCode.UNAUTHORIZED.businessException

        val user = userProfileRepository.findByUserId(ObjectId(userId))
            ?: throw UserErrorCode.NOT_FOUND_USER.businessException

        return UserProfileDto.from(user)
    }

    @Post("/nickname")
    @ProducesJson
    fun updateNickname(
        @Valid
        req: Nickname,
        ctx: ServiceRequestContext
    ): UserProfileDto {
        val nickname = req.nickname

        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))
            ?: throw UserErrorCode.UNAUTHORIZED.businessException

        val user = userProfileRepository.findByUserId(ObjectId(userId))
            ?.let { collection -> collection.nickname = nickname; collection }
            ?: throw UserErrorCode.NOT_FOUND_USER.businessException

        return UserProfileDto.from(userProfileRepository.save(user))
    }

    @Post("/status-message")
    @ProducesJson
    fun updateStatusMessage(
        @Valid
        req: StatusMessage,
        ctx: ServiceRequestContext
    ): UserProfileDto {
        val statusMessage = req.statusMessage

        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))
            ?: throw UserErrorCode.UNAUTHORIZED.businessException

        val user = userProfileRepository.findByUserId(ObjectId(userId))
            ?.let { collection -> collection.statusMessage = statusMessage; collection }
            ?: throw UserErrorCode.NOT_FOUND_USER.businessException

        return UserProfileDto.from(userProfileRepository.save(user))
    }

    @Post("/profile-image")
    @ProducesJson
    @Consumes(MediaTypeNames.MULTIPART_FORM_DATA)
    suspend fun updateProfileImage(
        @Valid
        req: ProfileImage,
        ctx: ServiceRequestContext
    ): UserProfileDto {
        val profileImage = req.profileImage

        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))
            ?: throw UserErrorCode.UNAUTHORIZED.businessException

        val user = withContext(Dispatchers.IO) {
            userProfileRepository.findByUserId(ObjectId(userId))
        } ?: throw UserErrorCode.NOT_FOUND_USER.businessException

        user.profileImageFileId = FileGrpcClient.uploadProfile(profileImage.file(), userId)

        return UserProfileDto.from(userProfileRepository.save(user))
    }
}
    