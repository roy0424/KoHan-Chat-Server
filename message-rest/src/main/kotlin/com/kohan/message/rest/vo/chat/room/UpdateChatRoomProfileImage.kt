package com.kohan.message.rest.vo.chat.room

import com.linecorp.armeria.common.multipart.MultipartFile
import com.linecorp.armeria.server.annotation.Param
import jakarta.validation.constraints.NotBlank

class UpdateChatRoomProfileImage(
    @Param
    @field:NotBlank(message = "Please enter a chat room id.")
    val chatRoomId: String,

    @Param
    val profileImage: MultipartFile
) {

}
