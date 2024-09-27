package com.kohan.message.rest.dto

import com.kohan.shared.collection.message.MessageCollection

class MessageDto(
    var id: String,
    var content: Any,
    var sender: String,
    var toReply: String,
    var reactions: List<ReactionDto>,
    var readUsers: List<String>,
) {
    companion object {
        fun from(message: MessageCollection): MessageDto =
            MessageDto(
                id = message.id.toString(),
                content = message.content,
                sender = message.sender.toString(),
                toReply = message.toReply?.toString() ?: "",
                reactions = message.reactions.map { ReactionDto.from(it) },
                readUsers = message.readUsers.map { it.toString() },
            )
    }

}
