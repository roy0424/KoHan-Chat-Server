package com.kohan.message.rest.collection.message

import com.kohan.message.rest.collection.message.item.Reaction
import com.kohan.shared.spring.mongo.collection.base.BaseCollection
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collation = "message")
class MessageCollection(
    /** 메시지 내용 */
    var content: Any,
    /** 보낸사람 */
    var sender: ObjectId,
    /** 채팅방 키 */
    @Indexed
    var chatRoom: ObjectId,
    /** 답장할 메시지 키 */
    var toReply: ObjectId?,
    /** 메시지 반응 목록 */
    var reactions: MutableList<Reaction>,
) : BaseCollection()
