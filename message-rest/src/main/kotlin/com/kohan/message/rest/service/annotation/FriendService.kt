package com.kohan.message.rest.service.annotation

import com.kohan.message.rest.dto.chat.UserProfileDto
import com.kohan.message.rest.dto.friend.FriendDto
import com.kohan.message.rest.exception.code.UserErrorCode
import com.kohan.message.rest.repository.friend.FriendRepository
import com.kohan.message.rest.repository.user.profile.UserProfileRepository
import com.kohan.message.rest.vo.friend.CreateFriendRequest
import com.kohan.message.rest.vo.friend.DeleteFriendVo
import com.kohan.message.rest.vo.friend.FriendStatusVo
import com.kohan.shared.armeria.exception.handler.BusinessExceptionHandler
import com.kohan.shared.collection.friend.FriendStatus
import com.kohan.shared.spring.exception.handler.ConstraintViolationExceptionHandler
import com.kohan.shared.spring.exception.handler.MismatchedInputExceptionHandler
import com.kohan.shared.spring.validator.ValidEnum
import com.linecorp.armeria.server.ServiceRequestContext
import com.linecorp.armeria.server.annotation.ExceptionHandler
import com.linecorp.armeria.server.annotation.Get
import com.linecorp.armeria.server.annotation.Param
import com.linecorp.armeria.server.annotation.Post
import com.linecorp.armeria.server.annotation.ProducesJson
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
    @ProducesJson
    fun createFriend(
        @Valid
        req: CreateFriendRequest,
        ctx: ServiceRequestContext,
    ): UserProfileDto {
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!

        validateRequestUser(ObjectId(userId), ObjectId(req.userId))

        friendRepository.findByDeleteAtIsNullAndFromUserIdAndToUserId(
            ObjectId(userId),
            ObjectId(req.friendId),
        )?.let {
            throw UserErrorCode.ALREADY_FRIEND.businessException
        }

        val friend =
            userProfileRepository.findByUserId(ObjectId(req.friendId))?: throw UserErrorCode.NOT_FOUND_USER.businessException

        friendRepository.save(req.toFriendCollection(userId, req.friendId))

        return UserProfileDto.from(friend)
    }

    @Get("/{fromUserId}")
    @ProducesJson
    fun getFriends(
        @Param("fromUserId")
        fromUserId: String,
        @Param("status")
        status: List<FriendStatus>,
        ctx: ServiceRequestContext,
    ): List<UserProfileDto> {
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!

        validateRequestUser(ObjectId(userId), ObjectId(fromUserId))

        val friends =
            friendRepository.findAllByDeleteAtIsNullAndFromUserIdAndStatusIn(
                ObjectId(fromUserId),
                status,
            )

        val friendIds = friends.map { it.toUserId }

        return userProfileRepository.findAllByUserIdInAndDeleteAtIsNullOrderByNicknameAsc(friendIds).map {
            UserProfileDto.from(it)
        }
    }

    @Post("/status")
    @ProducesJson
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

    @Post("/delete")
    @ProducesJson
    fun deleteFriend(
        @Valid
        deleteFriendVo: DeleteFriendVo,
        ctx: ServiceRequestContext,
    ) {
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!

        val friend =
            friendRepository.findById(ObjectId(deleteFriendVo.id)).orElseThrow {
                UserErrorCode.NOT_FOUND_FRIEND.businessException
            }

        validateRequestUser(ObjectId(userId), friend.fromUserId)

        friend.delete()
        friendRepository.save(friend)
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
