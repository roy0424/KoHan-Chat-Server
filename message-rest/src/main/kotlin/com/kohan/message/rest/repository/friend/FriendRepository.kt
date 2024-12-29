package com.kohan.message.rest.repository.friend

import com.kohan.shared.collection.friend.FriendCollection
import com.kohan.shared.collection.friend.FriendStatus
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface FriendRepository : MongoRepository<FriendCollection, ObjectId> {
    fun findAllByDeleteAtIsNullAndFromUserIdAndStatusIn(
        fromUserId: ObjectId,
        status: List<FriendStatus>,
    ): List<FriendCollection>

    fun findByDeleteAtIsNullAndFromUserIdAndToUserId(
        fromUserId: ObjectId,
        toUserId: ObjectId,
    ): FriendCollection?
}
