package com.kohan.message.rest.service.annotation

import com.kohan.message.rest.dto.chat.ChatRoomDto
import com.kohan.message.rest.dto.chat.ChatRoomWithLatestMessageInfoDto
import com.kohan.message.rest.dto.chat.MessageDto
import com.kohan.message.rest.exception.code.UserErrorCode
import com.kohan.message.rest.repository.chat.room.ChatRoomRepository
import com.kohan.message.rest.repository.message.CustomMessageRepository
import com.kohan.message.rest.repository.message.MessageRepository
import com.kohan.message.rest.util.addItemInList
import com.kohan.message.rest.util.isExpiredAfterDuration
import com.kohan.message.rest.util.transformList
import com.kohan.message.rest.vo.chat.room.CreateChatRoom
import com.kohan.message.rest.vo.chat.room.InviteChatRoom
import com.kohan.message.rest.vo.chat.room.LeaveChatRoom
import com.kohan.message.rest.vo.chat.room.UpdateChatRoomName
import com.kohan.message.rest.vo.chat.room.UpdateChatRoomProfileImage
import com.kohan.message.rest.vo.message.AddReaction
import com.kohan.message.rest.vo.message.DeleteMessage
import com.kohan.message.rest.vo.message.DeleteReaction
import com.kohan.message.rest.vo.message.SendMessage
import com.kohan.shared.armeria.exception.handler.BusinessExceptionHandler
import com.kohan.shared.collection.chatRoom.ChatRoomCollection
import com.kohan.shared.collection.chatRoom.ChatRoomType
import com.kohan.shared.collection.message.item.Reaction
import com.kohan.shared.collection.message.item.ReactionType
import com.kohan.shared.spring.exception.handler.ConstraintViolationExceptionHandler
import com.kohan.shared.spring.exception.handler.MismatchedInputExceptionHandler
import com.linecorp.armeria.server.ServiceRequestContext
import com.linecorp.armeria.server.annotation.ExceptionHandler
import com.linecorp.armeria.server.annotation.Get
import com.linecorp.armeria.server.annotation.Param
import com.linecorp.armeria.server.annotation.Post
import com.linecorp.armeria.server.annotation.ProducesJson
import io.netty.util.AttributeKey
import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import java.util.Optional

@Service
@Validated
@ExceptionHandler(BusinessExceptionHandler::class)
@ExceptionHandler(ConstraintViolationExceptionHandler::class)
@ExceptionHandler(MismatchedInputExceptionHandler::class)
class ChatService(
    private val chatRoomRepository: ChatRoomRepository,
    private val messageRepository: MessageRepository,
    private val customMessageRepository: CustomMessageRepository,
) {
    /**
     * 라인:
     * 개인 채팅방 만들시에는 메시지 보내기 전까지 채팅방이 생성되지 않는다.
     * 근데 단체 채팅방은 미리 생성되어 있어야 한다.
     * 카톡:
     * 메시지 보내기전 까지 방생성 x
     **/
    @Get("/chat-rooms")
    @ProducesJson
    fun getChatRoomList(
        @Param("page")
        page: Int = 0,
        @Param("size")
        size: Int = 10,
        ctx: ServiceRequestContext,
    ): Page<ChatRoomWithLatestMessageInfoDto> {
        // 유저가 안읽은 메시지 챗룸 아이디 distinct 로 종합해서 필요한 챗룸만 불러오기
        // 최적화 필요
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("latestMessage.createAt")))
        val chatRoomList = chatRoomRepository.findAllByUserListContainsAndDeleteAtIsNull(ObjectId(userId), pageable)

        val chatRoomWithLatestMessageInfoList =
            chatRoomList.map { chatRoom ->
                val latestMessageInfo = customMessageRepository.findLatestMessageAndUnreadCount(chatRoom._id, ObjectId(userId))
                ChatRoomWithLatestMessageInfoDto.from(chatRoom, latestMessageInfo)
            }

        return chatRoomWithLatestMessageInfoList
    }

    // 채팅방 조회
    @Get("/chat-rooms/{chatRoomId}")
    @ProducesJson
    fun getChatRoom(
        @Param("chatRoomId")
        chatRoomId: String,
        ctx: ServiceRequestContext,
    ): ChatRoomDto {
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!
        val chatRoom = getValidChatRoomWithUserId(chatRoomRepository.findById(ObjectId(chatRoomId)), userId)

        return ChatRoomDto.from(chatRoom)
    }

    // 히스토리 조회
    @Get("/chat-rooms/{chatRoomId}/messages/{latestReadMessageId}/history")
    @ProducesJson
    fun getChatRoomMessages(
        @Param("chatRoomId")
        chatRoomId: String,
        @Param("latestReadMessageId")
        messageId: String,
        ctx: ServiceRequestContext,
    ): List<MessageDto> {
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!

        if (!chatRoomRepository.existsByIdAndUserListContains(ObjectId(chatRoomId), ObjectId(userId))) {
            throw UserErrorCode.NOT_IN_CHAT_ROOM.businessException
        }

        val messageList =
            messageRepository.findAllByChatRoomIdAndDeleteAtIsNullAndIdGreaterThanOrderByIdAsc(ObjectId(chatRoomId), ObjectId(messageId))

        return transformList(messageList) { MessageDto.from(it) }
    }

    // 채팅방 생성
    @Post("/chat-rooms/create")
    @ProducesJson
    fun createChatRoom(
        @Valid
        req: CreateChatRoom,
        ctx: ServiceRequestContext,
    ): ChatRoomDto {
        /**
         * todo 채팅방 생성 로직 추가
         * 1. 채팅방 생성
         * 2. 메시지 생성
         * 3. kafka 로 메시지 전달
         **/
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!

        if (!req.userList.contains(userId)) {
            throw UserErrorCode.REQUEST_USER_NOT_IN_CHAT_ROOM.businessException
        }

        val chatRoom = chatRoomRepository.save(req.toChatRoomCollection())
        messageRepository.save(req.toMessageCollection(chatRoom._id))

        // todo kafka 로 메시지 전달

        return ChatRoomDto.from(chatRoom)
    }

    // 채팅방 이름 업데이트
    @Post("/chat-rooms/update/name")
    @ProducesJson
    fun updateChatRoomName(
        @Valid
        req: UpdateChatRoomName,
        ctx: ServiceRequestContext,
    ): ChatRoomDto {
        /**
         * todo 채팅방 업데이트 로직 추가
         * 1. 채팅방 업데이트
         * 2. kafka 로 메시지 전달
         **/
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!
        val chatRoom = getValidChatRoomWithUserId(chatRoomRepository.findById(ObjectId(req.chatRoomId)), userId)
        chatRoom.name = req.name
        // todo kafka 로 메시지 전달

        return ChatRoomDto.from(chatRoomRepository.save(chatRoom))
    }

    @Post("/chat-rooms/update/profile-image")
    @ProducesJson
    fun updateChatRoomProfileImage(
        @Valid
        req: UpdateChatRoomProfileImage,
        ctx: ServiceRequestContext,
    ): ChatRoomDto {
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!
        val chatRoom = getValidChatRoomWithUserId(chatRoomRepository.findById(ObjectId(req.chatRoomId)), userId)

        // todo file grpc uploadChatRoomProfileImage or 파일서버에 올리고 아이디 받아오기

        // todo kafka 로 메시지 전달

        return ChatRoomDto.from(chatRoom)
    }

    // 채팅방 초대
    @Post("/chat-rooms/invite")
    @ProducesJson
    fun inviteChatRoom(
        @Valid
        req: InviteChatRoom,
        ctx: ServiceRequestContext,
    ): ChatRoomDto {
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!
        val chatRoom = getValidChatRoomWithUserId(chatRoomRepository.findById(ObjectId(req.chatRoomId)), userId)

        return chatRoom
            .takeIf { it.type == ChatRoomType.GROUP }
            ?.takeUnless { it.userList.contains(ObjectId(req.userId)) }
            ?.let {
                val updatedUserList = addItemInList(it.userList, ObjectId(req.userId))
                val updatedChatRoom =
                    it.apply {
                        userList = updatedUserList.toMutableList()
                    }
                chatRoomRepository.save(updatedChatRoom)
            }?.let(ChatRoomDto::from)
            ?: throw when {
                chatRoom.type != ChatRoomType.GROUP ->
                    UserErrorCode.NOT_GROUP_CHAT_ROOM.businessException
                else -> UserErrorCode.ALREADY_IN_CHAT_ROOM.businessException
            }
    }

    // 채팅방 나가기
    @Post("/chat-rooms/leave")
    @ProducesJson
    fun leaveChatRoom(
        @Valid
        req: LeaveChatRoom,
        ctx: ServiceRequestContext,
    ): ChatRoomDto {
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!

        if (req.userId != userId) {
            throw UserErrorCode.USER_NOT_LEAVING_USER.businessException
        }

        return getValidChatRoomWithUserId(chatRoomRepository.findById(ObjectId(req.chatRoomId)), userId)
            .let { chatRoom ->
                when (chatRoom.userList.size) {
                    1 -> {
                        chatRoom.delete()
                        val updatedChatRoom = chatRoom.apply { userList = mutableListOf() }
                        chatRoomRepository.save(updatedChatRoom)
                        ChatRoomDto.from(updatedChatRoom)
                    }

                    else -> {
                        val updatedChatRoom =
                            chatRoom.apply {
                                userList = chatRoom.userList.filterNot { it == ObjectId(userId) }.toMutableList()
                            }
                        chatRoomRepository.save(updatedChatRoom)
                        ChatRoomDto.from(updatedChatRoom)
                    }
                }
            }
    }

    // 메시지 전송
    @Post("/messages/send")
    @ProducesJson
    fun sendMessage(
        @Valid
        req: SendMessage,
        ctx: ServiceRequestContext,
    ): MessageDto {
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!

        validateSender(userId, req.sender)

        if (!chatRoomRepository.existsById(ObjectId(req.chatRoomId))) {
            throw UserErrorCode.NOT_FOUND_CHAT_ROOM.businessException
        }

        if (!chatRoomRepository.existsByIdAndUserListContains(ObjectId(req.chatRoomId), ObjectId(userId))) {
            throw UserErrorCode.NOT_IN_CHAT_ROOM.businessException
        }

        val message = messageRepository.save(req.toMessageCollection())

        // todo kafka 로 메시지 전달

        return MessageDto.from(message)
    }

    // 메시지 삭제
    @Post("/messages/delete")
    @ProducesJson
    fun deleteMessage(
        @Valid
        req: DeleteMessage,
        ctx: ServiceRequestContext,
    ) {
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!

        val message =
            messageRepository
                .findById(ObjectId(req.messageId))
                .orElseThrow { UserErrorCode.NOT_FOUND_MESSAGE.businessException }
                .apply {
                    validateSender(userId, sender.toString())
                    require(!isExpiredAfterDuration(createAt!!, 5)) { throw UserErrorCode.MESSAGE_NOT_DELETABLE.businessException }
                }

        // todo kafka 로 메시지 전달

        message.delete()
        messageRepository.save(message)
    }

    // 리액션 달기
    @Post("/messages/reactions/add")
    @ProducesJson
    fun addReaction(
        @Valid
        req: AddReaction,
        ctx: ServiceRequestContext,
    ): MessageDto {
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!

        validateSender(userId, req.sender)

        return messageRepository
            .findById(ObjectId(req.messageId))
            .map { message ->
                val existingReaction = message.reactions.find { it.senderUser == ObjectId(userId) }

                val updatedReactions =
                    existingReaction?.let { reaction ->
                        message.reactions.map {
                            if (reaction.senderUser == ObjectId(req.sender)) {
                                reaction.apply { type = ReactionType.valueOf(req.type) }
                            } else {
                                reaction
                            }
                        }
                    } ?: addItemInList(message.reactions, Reaction(ObjectId(req.sender), ReactionType.valueOf(req.type)))
                message.apply {
                    reactions = updatedReactions.toMutableList()
                }
            }.map { updatedMessage ->
                messageRepository.save(updatedMessage)
                MessageDto.from(updatedMessage)
            }.orElseThrow { throw UserErrorCode.NOT_FOUND_MESSAGE.businessException }
    }

    // 리액션 삭제
    @Post("/messages/reactions/delete")
    @ProducesJson
    fun deleteReaction(
        @Valid
        req: DeleteReaction,
        ctx: ServiceRequestContext,
    ): MessageDto {
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!

        validateSender(userId, req.sender)

        return messageRepository
            .findById(ObjectId(req.messageId))
            .map { message ->
                val existingReaction = message.reactions.find { it.senderUser == ObjectId(userId) }

                val updatedReactions =
                    existingReaction?.let {
                        message.reactions.filterNot { it.senderUser == ObjectId(req.sender) }
                    } ?: message.reactions
                message.apply {
                    reactions = updatedReactions.toMutableList()
                }
            }.map { updatedMessage ->
                messageRepository.save(updatedMessage)
                MessageDto.from(updatedMessage)
            }.orElseThrow { throw UserErrorCode.NOT_FOUND_MESSAGE.businessException }
    }

    private fun getValidChatRoomWithUserId(
        chatRoom: Optional<ChatRoomCollection>,
        userId: String,
    ): ChatRoomCollection {
        chatRoom
            .orElseThrow { UserErrorCode.NOT_FOUND_CHAT_ROOM.businessException }
            ?.takeIf { it.userList.contains(ObjectId(userId)) }
            ?: throw UserErrorCode.NOT_IN_CHAT_ROOM.businessException

        return chatRoom.get()
    }

    private fun validateSender(
        userId: String,
        sender: String,
    ) {
        require(userId == sender) { throw UserErrorCode.USER_NOT_SENDER.businessException }
    }
}
