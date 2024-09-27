package com.kohan.message.rest.service.annotation

import com.kohan.message.rest.dto.ChatRoomDto
import com.kohan.message.rest.exception.code.UserErrorCode
import com.kohan.message.rest.repository.chat.room.ChatRoomRepository
import com.kohan.message.rest.repository.message.MessageRepository
import com.kohan.message.rest.dto.MessageDto
import com.kohan.message.rest.vo.chat.room.*
import com.kohan.shared.armeria.exception.handler.BusinessExceptionHandler
import com.kohan.shared.collection.chatRoom.ChatRoomCollection
import com.kohan.shared.enum.chatRoom.ChatRoomType
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
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Service
@Validated
@ExceptionHandler(BusinessExceptionHandler::class)
@ExceptionHandler(ConstraintViolationExceptionHandler::class)
@ExceptionHandler(MismatchedInputExceptionHandler::class)
class ChatService(
    private val chatRoomRepository: ChatRoomRepository,
    private val messageRepository: MessageRepository
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
        ctx: ServiceRequestContext
    ): List<ChatRoomDto> {
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!
        val chatRoomList = chatRoomRepository.findByUserListContains(ObjectId(userId))
        /**
         * todo 채팅방에 마지막 메시지 및 쌓인 메시지 수를 가져오는 로직 추가
         * aggregate 로 최적화 가능할 것으로 보임
         * 대신 코드가 복잡해질 수 있음
         * aggregate 로 최적화하지 않으면
         * 채팅방 100개 조회하면 1 + 100(latestMessage) + 100(unreadMessagesCount) = 201번의 쿼리가 나감
         * 그리고 카톡 방식으로 해야 latestMessage nullable 하지 않음
         * 조회할때 pagination 할것인지
         **/
        return transformList(chatRoomList) { ChatRoomDto.from(it) }
    }

    // 채팅방 조회
    @Get("/chat-rooms/{chatRoomId}")
    @ProducesJson
    fun getChatRoom(
        @Param("chatRoomId")
        chatRoomId: String,
        ctx: ServiceRequestContext
    ): ChatRoomDto {
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!
        val chatRoom = getChatRoomWithUserId(chatRoomId, userId)

        return ChatRoomDto.from(chatRoom)
    }

    // 히스토리 조회
    @Get("/chat-rooms/{chatRoomId}/messages/{latestReadMessageId}")
    @ProducesJson
    fun getChatRoomMessages(
        @Param("chatRoomId")
        chatRoomId: String,
        @Param("latestReadMessageId")
        messageId: String,
        ctx: ServiceRequestContext
    ): List<MessageDto> {
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!

        if (!chatRoomRepository.existsByIdAndUserListContains(ObjectId(chatRoomId), ObjectId(userId))) {
            throw UserErrorCode.NOT_IN_CHAT_ROOM.businessException
        }

        val messageList =
            messageRepository.findByChatRoomIdAndIdGreaterThanOrderByIdAsc(ObjectId(chatRoomId), ObjectId(messageId))

        return transformList(messageList) { MessageDto.from(it) }
    }

    // 채팅방 생성
    @Post("/chat-rooms/create")
    @ProducesJson
    fun createChatRoom(
        @Valid
        req: CreateChatRoom,
        ctx: ServiceRequestContext
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
        val message = messageRepository.save(req.toMessageCollection(chatRoom._id))

        // todo kafka 로 메시지 전달

        return ChatRoomDto.from(chatRoom)
    }

    // 채팅방 이름 업데이트
    @Post("/chat-rooms/update/name")
    @ProducesJson
    fun updateChatRoomName(
        @Valid
        req: UpdateChatRoomName,
        ctx: ServiceRequestContext
    ): ChatRoomDto {
        /**
         * todo 채팅방 업데이트 로직 추가
         * 1. 채팅방 업데이트
         * 2. kafka 로 메시지 전달
         **/
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!
        val chatRoom = getChatRoomWithUserId(req.chatRoomId, userId)

        chatRoom.name = req.name
        val updatedChatRoom = chatRoomRepository.save(chatRoom)

        // todo kafka 로 메시지 전달

        return ChatRoomDto.from(updatedChatRoom)
    }

    @Post("/chat-rooms/update/profile-image")
    @ProducesJson
    fun updateChatRoomProfileImage(
        @Valid
        req: UpdateChatRoomProfileImage,
        ctx: ServiceRequestContext
    ): ChatRoomDto {
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!
        val chatRoom = getChatRoomWithUserId(req.chatRoomId, userId)

        // todo file grpc uploadChatRoomProfileImage

        // todo kafka 로 메시지 전달

        return ChatRoomDto.from(chatRoom)
    }

    // 채팅방 초대
    @Post("/chat-rooms/invite")
    @ProducesJson
    fun inviteChatRoom(
        @Valid
        req: InviteChatRoom,
        ctx: ServiceRequestContext
    ): ChatRoomDto {
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!

        return getChatRoomWithUserId(req.chatRoomId, userId)
            .takeIf { it.type == ChatRoomType.GROUP }
            ?.takeUnless { it.userList.contains(ObjectId(req.userId)) }
            ?.let { chatRoom ->
                val updatedUserList = chatRoom.userList + ObjectId(req.userId)
                val updatedChatRoom = chatRoom.apply {
                    userList = updatedUserList.toMutableList()
                }
                chatRoomRepository.save(updatedChatRoom)
            }?.let(ChatRoomDto::from)
            ?: throw when {
                getChatRoomWithUserId(req.chatRoomId, userId).type != ChatRoomType.GROUP ->
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
        ctx: ServiceRequestContext
    ): ChatRoomDto {
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!

        if (req.userId != userId) {
            throw UserErrorCode.USER_NOT_LEAVING_USER.businessException
        }

        return getChatRoomWithUserId(req.chatRoomId, userId)
            .let { chatRoom ->
                when (chatRoom.userList.size) {
                    1 -> {
                        chatRoom.delete()
                        val updatedChatRoom = chatRoom.apply { userList = mutableListOf() }
                        chatRoomRepository.save(updatedChatRoom)
                        ChatRoomDto.from(updatedChatRoom)
                    }
                    else -> {
                        val updatedChatRoom = chatRoom.apply {
                            userList = chatRoom.userList.filterNot { it == ObjectId(userId) }.toMutableList()
                        }
                        chatRoomRepository.save(updatedChatRoom)
                        ChatRoomDto.from(updatedChatRoom)
                    }
                }
            }
    }

    // 메시지 전송

    // 메시지 삭제

    // 리액션 달기

    // 리액션 삭제


    /**
     * 함수형 보고 영감받은거
     */
    private fun <T1, T2> transformList(list: List<T1>, transform: (T1) -> T2): List<T2> {
        return list.map { transform(it) }
    }

    private fun getChatRoomWithUserId(chatRoomId: String, userId: String): ChatRoomCollection {
        return chatRoomRepository.findById(ObjectId(chatRoomId))
            .orElseThrow {
                UserErrorCode.NOT_FOUND_CHAT_ROOM.businessException
            }?.takeIf {
                it.userList.contains(ObjectId(userId))
            } ?: throw UserErrorCode.NOT_IN_CHAT_ROOM.businessException
    }
}