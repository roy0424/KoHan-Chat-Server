package com.kohan.message.rest.service.annotation

import com.kohan.message.rest.dto.chat.UserProfileDto
import com.kohan.message.rest.exception.code.UserErrorCode
import com.kohan.message.rest.repository.friend.FriendRepository
import com.kohan.message.rest.repository.user.profile.UserProfileRepository
import com.kohan.message.rest.vo.friend.CreateFriendRequest
import com.kohan.shared.armeria.exception.handler.BusinessExceptionHandler
import com.kohan.shared.spring.exception.handler.ConstraintViolationExceptionHandler
import com.kohan.shared.spring.exception.handler.MismatchedInputExceptionHandler
import com.linecorp.armeria.server.ServiceRequestContext
import com.linecorp.armeria.server.annotation.ExceptionHandler
import com.linecorp.armeria.server.annotation.Post
import io.netty.util.AttributeKey
import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Service
@Validated
@ExceptionHandler(BusinessExceptionHandler::class)
@ExceptionHandler(ConstraintViolationExceptionHandler::class)
@ExceptionHandler(MismatchedInputExceptionHandler::class)
class FriendService(
    private val friendRepository: FriendRepository,
    private val userProfileRepository: UserProfileRepository,
) {
    @Post("/create")
    fun createFriend(
        @Valid
        req: CreateFriendRequest,
        ctx: ServiceRequestContext,
    ): UserProfileDto {
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!

        validateRequestUser(ObjectId(userId), ObjectId(req.userId))

        val friend =
            userProfileRepository.findByUserId(ObjectId(req.friendId))
                ?: throw UserErrorCode.NOT_FOUND_USER.businessException

        friendRepository.save(req.toFriendCollection(userId, req.friendId))

        return UserProfileDto.from(friend)
    }

    private fun validateRequestUser(
        userId: ObjectId,
        reqUserId: ObjectId,
    ) {
        if (userId != reqUserId) {
            throw UserErrorCode.USER_NOT_REQ_USER.businessException
        }
    }
}
