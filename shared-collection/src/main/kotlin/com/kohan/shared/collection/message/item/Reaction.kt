package com.kohan.shared.collection.message.item

import org.bson.types.ObjectId

class Reaction(
    /** 반응한 사람 */
    var senderUser: ObjectId,
    /** 반응 종류 */
    var type: ReactionType,
)
