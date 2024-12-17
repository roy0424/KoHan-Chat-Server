package com.kohan.message.rest.vo.friend

import com.kohan.shared.collection.friend.FriendCollection
import com.kohan.shared.collection.friend.FriendStatus
import jakarta.validation.constraints.NotNull
import org.bson.types.ObjectId

class CreateFriendRequest(
    @field:NotNull(message = "Please enter a user ID.")
    val userId: String,
    @field:NotNull(message = "Please enter a friend ID.")
    val friendId: String,
) {
    fun toFriendCollection(
        userId: String,
        friendId: String,
    ): FriendCollection =
        FriendCollection(
            fromUserId = ObjectId(userId),
            toUserId = ObjectId(friendId),
            status = FriendStatus.NORMAL,
        )
}
