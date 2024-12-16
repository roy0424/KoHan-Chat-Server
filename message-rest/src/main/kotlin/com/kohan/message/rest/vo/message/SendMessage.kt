package com.kohan.message.rest.vo.message

import com.kohan.shared.collection.message.MessageCollection
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.bson.types.ObjectId

class SendMessage(
    @field:NotNull(message = "Please enter a content.")
    val content: Any,
    @field:NotBlank(message = "Please enter a sender id.")
    val sender: String,
    @field:NotBlank(message = "Please enter a chat room id.")
    val chatRoomId: String,
    val toReply: String? = null,
) {
    fun toMessageCollection(): MessageCollection =
        MessageCollection(
            content = content,
            sender = ObjectId(sender),
            chatRoomId = ObjectId(chatRoomId),
            toReply = toReply?.let { ObjectId(it) },
            reactions = mutableListOf(),
            readUsers = mutableListOf(),
        )
}
