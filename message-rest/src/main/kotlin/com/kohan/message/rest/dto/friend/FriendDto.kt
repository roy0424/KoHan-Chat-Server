package com.kohan.message.rest.dto.friend

import com.kohan.shared.collection.friend.FriendCollection
import com.kohan.shared.collection.friend.FriendStatus

class FriendDto(
    var id: String,
    var userId: String,
    var friendId: String,
    var status: FriendStatus,
) {
    companion object {
        fun from(friendCollection: FriendCollection): FriendDto =
            FriendDto(
                id = friendCollection.id!!.toHexString(),
                userId = friendCollection.fromUserId.toHexString(),
                friendId = friendCollection.toUserId.toHexString(),
                status = friendCollection.status,
            )
    }
}
