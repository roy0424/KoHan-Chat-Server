package com.kohan.shared.collection.friend

import com.kohan.shared.collection.base.BaseCollection
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "friend")
class FriendCollection(
    /** 친구 요청을 보낸 유저 ID */
    var fromUserId: ObjectId,
    /** 친구 요청을 보낸 유저 ID */
    var toUserId: ObjectId,
    /** 친구 상태 */
    var status: FriendStatus,
) : BaseCollection()
