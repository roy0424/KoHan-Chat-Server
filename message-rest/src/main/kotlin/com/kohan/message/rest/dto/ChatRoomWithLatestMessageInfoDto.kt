package com.kohan.message.rest.dto

import com.kohan.message.rest.vo.message.LatestMessageInfo
import com.kohan.shared.collection.chatRoom.ChatRoomCollection
import com.kohan.shared.enum.chatRoom.ChatRoomType

class ChatRoomWithLatestMessageInfoDto(
    var id: String,
    var name: String,
    var profileImageFileId: String,
    var type: ChatRoomType,
    var userList: List<String>,
    var latestMessageInfo: LatestMessageInfo,
) {
    companion object {
        fun from(
            chatRoom: ChatRoomCollection,
            latestMessageInfo: LatestMessageInfo,
        ): ChatRoomWithLatestMessageInfoDto =
            ChatRoomWithLatestMessageInfoDto(
                id = chatRoom._id.toHexString(),
                name = chatRoom.name,
                profileImageFileId = chatRoom.profileImageFileId.toString(),
                type = chatRoom.type,
                userList = chatRoom.userList.map { it.toString() },
                latestMessageInfo = latestMessageInfo,
            )
    }
}
