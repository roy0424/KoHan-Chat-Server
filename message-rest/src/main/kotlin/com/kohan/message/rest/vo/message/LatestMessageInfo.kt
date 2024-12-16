package com.kohan.message.rest.vo.message

import com.kohan.shared.collection.message.MessageCollection

class LatestMessageInfo(
    val id: String,
    val content: Any,
    val createAt: String,
    val unreadCount: Int,
) {
    companion object {
        fun from(message: MessageCollection): LatestMessageInfo =
            LatestMessageInfo(
                id = message._id.toHexString(),
                content = message.content,
                createAt = message.createAt.toString(),
                unreadCount = message.readUsers.size,
            )
    }
}
