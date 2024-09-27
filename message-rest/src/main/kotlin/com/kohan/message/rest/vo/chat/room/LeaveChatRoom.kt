package com.kohan.message.rest.vo.chat.room

import jakarta.validation.constraints.NotBlank

class LeaveChatRoom(
    @field:NotBlank(message = "Please enter a chat room id.")
    val chatRoomId: String,

    @field:NotBlank(message = "Please enter a user id.")
    val userId: String
) {

}
