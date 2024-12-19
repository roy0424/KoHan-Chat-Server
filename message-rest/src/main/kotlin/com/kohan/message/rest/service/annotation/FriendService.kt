package com.kohan.message.rest.service.annotation

import com.kohan.message.rest.dto.chat.UserProfileDto
import com.kohan.message.rest.dto.friend.FriendDto
import com.kohan.message.rest.exception.code.UserErrorCode
import com.kohan.message.rest.repository.friend.FriendRepository
import com.kohan.message.rest.repository.user.profile.UserProfileRepository
import com.kohan.message.rest.vo.friend.CreateFriendRequest
import com.kohan.message.rest.vo.friend.FriendStatusVo
import com.kohan.shared.armeria.exception.handler.BusinessExceptionHandler
import com.kohan.shared.collection.friend.FriendStatus
import com.kohan.shared.spring.exception.handler.ConstraintViolationExceptionHandler
import com.kohan.shared.spring.exception.handler.MismatchedInputExceptionHandler
import com.linecorp.armeria.server.ServiceRequestContext
import com.linecorp.armeria.server.annotation.ExceptionHandler
import com.linecorp.armeria.server.annotation.Get
import com.linecorp.armeria.server.annotation.Param
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
            userProfileRepository.findById(ObjectId(req.friendId)).orElseThrow {
                UserErrorCode.NOT_FOUND_USER.businessException
            }

        friendRepository.save(req.toFriendCollection(userId, req.friendId))

        return UserProfileDto.from(friend)
    }

    @Get("/{fromUserId}")
    fun getFriends(
        @Param("fromUserId")
        fromUserId: String,
        ctx: ServiceRequestContext,
    ): List<UserProfileDto> {
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!

        validateRequestUser(ObjectId(userId), ObjectId(fromUserId))

        val friends =
            friendRepository.findAllByDeleteAtIsNullAndFromUserIdAndStatusIn(
                ObjectId(fromUserId),
                listOf(FriendStatus.NORMAL, FriendStatus.FAVORITE),
            )

        val friendIds = friends.map { it.toUserId }

        return userProfileRepository.findAllByIdInAndDeleteAtIsNullOrderByNicknameAsc(friendIds).map {
            UserProfileDto.from(it)
        }
    }

    // todo favorite만 조회
    @Post("/status")
    fun updateFriendStatus(
        @Valid
        friendStatusVo: FriendStatusVo,
        ctx: ServiceRequestContext,
    ): FriendDto {
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!
        val friend = friendRepository.findById(ObjectId(friendStatusVo.id)).orElseThrow { UserErrorCode.NOT_FOUND_FRIEND.businessException }

        validateRequestUser(ObjectId(userId), friend.fromUserId)

        friend.status = FriendStatus.valueOf(friendStatusVo.status)

        return FriendDto.from(friendRepository.save(friend))
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
