package com.kohan.message.rest.repository.chat.room

import com.kohan.shared.collection.chatRoom.ChatRoomCollection
import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface ChatRoomRepository : MongoRepository<ChatRoomCollection, ObjectId> {
    fun save(chatRoomCollection: ChatRoomCollection): ChatRoomCollection

    @Query("{ 'userList': ?0 }")
    fun findByUserListContainsAndDeleteAtNotNull(
        userId: ObjectId,
        pageable: Pageable,
    ): Page<ChatRoomCollection>

    @Query("{ '_id': ?0, userList: { \$all: [ ?1 ] } }", exists = true)
    fun existsByIdAndUserListContains(
        chatRoomId: ObjectId,
        userId: ObjectId,
    ): Boolean
}
