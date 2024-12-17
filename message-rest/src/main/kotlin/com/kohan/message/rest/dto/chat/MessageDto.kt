package com.kohan.message.rest.dto.chat

import com.kohan.shared.collection.message.MessageCollection

class MessageDto(
    var id: String,
    var content: Any,
    var sender: String,
    var chatRoomId: String,
    var toReply: String,
    var reactions: List<ReactionDto>,
    var readUsers: List<String>,
) {
    companion object {
        fun from(message: MessageCollection): MessageDto =
            MessageDto(
                id = message._id.toHexString(),
                content = message.content,
                sender = message.sender.toString(),
                chatRoomId = message.chatRoomId.toString(),
                toReply = message.toReply?.toString() ?: "",
                reactions = message.reactions.map { ReactionDto.from(it) },
                readUsers = message.readUsers.map { it.toString() },
            )
    }
}
