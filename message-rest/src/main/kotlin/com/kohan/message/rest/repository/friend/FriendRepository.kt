package com.kohan.message.rest.repository.friend

import com.kohan.shared.collection.friend.FriendCollection
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface FriendRepository: MongoRepository<FriendCollection, ObjectId> {
}