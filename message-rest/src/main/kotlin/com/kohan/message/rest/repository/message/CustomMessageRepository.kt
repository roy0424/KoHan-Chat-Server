package com.kohan.message.rest.repository.message

import com.kohan.message.rest.vo.message.LatestMessageInfo
import org.bson.types.ObjectId

interface CustomMessageRepository {
    fun findLatestMessageAndUnreadCount(
        chatRoomId: ObjectId,
        userId: ObjectId,
    ): LatestMessageInfo
}
