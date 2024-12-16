package com.kohan.message.rest.repository.message

import com.kohan.shared.collection.message.MessageCollection
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface MessageRepository : MongoRepository<MessageCollection, ObjectId> {
    @Query("{ chatRoomId: ?0, '_id': { \$gt: ?1 } }")
    fun findByChatRoomIdAndDeletedAtNotNullAndIdGreaterThanOrderByIdAsc(
        chatRoomId: ObjectId,
        messageId: ObjectId,
    ): List<MessageCollection>

    fun findTopByChatRoomIdAndDeleteAtNotNullOrderByCreateAtDesc(chatRoomId: ObjectId): MessageCollection
}
