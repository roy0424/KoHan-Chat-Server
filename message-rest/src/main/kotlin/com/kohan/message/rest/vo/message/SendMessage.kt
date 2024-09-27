package com.kohan.message.rest.vo.message

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

class SendMessage(
    @field:NotNull(message = "Please enter a content.")
    var content: Any,

    @field:NotBlank(message = "Please enter a sender id.")
    var sender: String,

    @field:NotBlank(message = "Please enter a chat room id.")
    var chatRoomId: String,

    var toReply: String?,
) {
}