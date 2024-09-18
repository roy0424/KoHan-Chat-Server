package com.kohan.shared.collection.chatRoom

import com.kohan.shared.collection.base.BaseCollection
import com.kohan.shared.enum.chatRoom.ChatRoomType
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.Document

@Document(collation = "chat-room")
class ChatRoomCollection(
    /** 채팅방 이름 */
    var name: String,
    /** 채팅방 프로필 이미지 경로 */
    var profileImageUrl: String,
    /** 채팅방 종류 */
    var type: ChatRoomType,
    /** 채팅방에 참여하고 있는 유저 목록 */
    var userList: MutableList<ObjectId>,
) : BaseCollection()
