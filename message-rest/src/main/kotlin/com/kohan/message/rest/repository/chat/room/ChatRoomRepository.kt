package com.kohan.message.rest.repository.chat.room

import com.kohan.shared.collection.chatRoom.ChatRoomCollection
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface ChatRoomRepository : MongoRepository<ChatRoomCollection, ObjectId> {
    fun save(chatRoomCollection: ChatRoomCollection): ChatRoomCollection

    fun findByUserListContains(userId: ObjectId): List<ChatRoomCollection>

    fun existsByIdAndUserListContains(chatRoomId: ObjectId, userId: ObjectId): Boolean
}