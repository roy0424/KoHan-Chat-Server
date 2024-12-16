package com.kohan.message.rest.repository.message

import com.kohan.message.rest.vo.message.LatestMessageInfo
import com.kohan.shared.collection.message.MessageCollection
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.where
import org.springframework.stereotype.Repository
import java.time.format.DateTimeFormatter

@Repository
class CustomMessageRepositoryImpl(
    private val mongoTemplate: MongoTemplate,
) : CustomMessageRepository {
    override fun findLatestMessageAndUnreadCount(
        chatRoomId: ObjectId,
        userId: ObjectId,
    ): LatestMessageInfo {
        val latestAndUnreadQuery =
            Query(
                Criteria
                    .where("chatRoomId")
                    .`is`(chatRoomId)
                    .and("deleteAt")
                    .isNull,
            ).with(Sort.by("createAt").descending())
                .limit(1000)

        val messages: List<MessageCollection> = mongoTemplate.find(latestAndUnreadQuery, MessageCollection::class.java)

        // 최신 메시지
        val latestMessage =
            messages.firstOrNull() ?: throw NoSuchElementException("No message found for chatRoomId: $chatRoomId")

        // 가장 최근에 읽은 메시지를 찾음
        val lastReadMessage = messages.firstOrNull { it.readUsers.contains(userId) }

        // 마지막 읽은 메시지 이후의 읽지 않은 메시지 개수를 계산
        val unreadMessageCount =
            messages.count {
                it.createAt!! > (lastReadMessage?.createAt ?: java.time.LocalDateTime.MIN) && !it.readUsers.contains(userId)
            }

        return LatestMessageInfo(
            id = latestMessage.id.toString(),
            content = latestMessage.content,
            createAt = latestMessage.createAt!!.format(DateTimeFormatter.ISO_DATE_TIME),
            unreadCount = unreadMessageCount,
        )
    }
}
