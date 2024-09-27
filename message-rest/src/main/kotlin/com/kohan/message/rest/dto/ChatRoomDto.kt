package com.kohan.message.rest.dto

import com.kohan.shared.collection.chatRoom.ChatRoomCollection
import com.kohan.shared.enum.chatRoom.ChatRoomType

data class ChatRoomDto(
    var id: String,
    var name: String,
    var profileImageFileId: String,
    var type: ChatRoomType,
    var userList: List<String>,
) {
    companion object {
        fun from(chatRoom: ChatRoomCollection): ChatRoomDto =
            ChatRoomDto(
                id = chatRoom.id.toString(),
                name = chatRoom.name,
                profileImageFileId = chatRoom.profileImageFileId.toString(),
                type = chatRoom.type,
                userList = chatRoom.userList.map { it.toString() },
            )
    }
}
