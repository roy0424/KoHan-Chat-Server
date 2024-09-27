package com.kohan.message.rest.vo.chat.room

import com.kohan.shared.collection.chatRoom.ChatRoomCollection
import com.kohan.shared.collection.message.MessageCollection
import com.kohan.shared.enum.chatRoom.ChatRoomType
import com.kohan.shared.spring.validator.ValidEnum
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.bson.types.ObjectId

class CreateChatRoom(
    @field:Size(max = 20, message = "Please enter a chat room name of no more than 20 characters.")
    @field:NotNull(message = "Please enter a chat room name.")
    val name: String,

    @field:NotNull(message = "Please enter a chat room profile image.")
    val profileImageFileId: String,

    @field:ValidEnum(
        message = "Invalid chat room type. This is not permitted.",
        enumClass = ChatRoomType::class
    )
    val type: ChatRoomType,

    @field:NotBlank(message = "Please enter a user list.")
    val userList: List<String>,

    // todo init message vo 추가
    @field:NotNull(message = "Please enter a init message.")
    val initMessage: InitMessage
) {
    fun toChatRoomCollection(): ChatRoomCollection {
        return ChatRoomCollection(
            name = name,
            profileImageFileId = ObjectId(profileImageFileId),
            type = type,
            userList = userList.map { ObjectId(it) }.toMutableList(),
        )
    }

    fun toMessageCollection(chatRoomId: ObjectId): MessageCollection {
        return MessageCollection(
            chatRoomId = chatRoomId,
            content = initMessage.content,
            sender = ObjectId(initMessage.sender),
            toReply = null,
            reactions = mutableListOf(),
            readUsers = mutableListOf(),
        )
    }
}
