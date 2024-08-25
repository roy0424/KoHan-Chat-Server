package com.kohan.message.rest.collection.message.item

import com.kohan.message.rest.enum.collection.message.item.ReactionType
import org.bson.types.ObjectId

class Reaction(
    /** 반응한 사람 */
    var senderUser: ObjectId,
    /** 반응 종류 */
    var type: ReactionType,
)
