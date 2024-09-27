package com.kohan.message.rest.repository.message

import com.kohan.shared.collection.message.MessageCollection
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface MessageRepository: MongoRepository<MessageCollection, ObjectId> {
    fun findByChatRoomIdAndIdGreaterThanOrderByIdAsc(chatRoomId: ObjectId, messageId: ObjectId): List<MessageCollection>
}