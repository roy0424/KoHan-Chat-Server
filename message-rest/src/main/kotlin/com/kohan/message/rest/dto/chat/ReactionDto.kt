package com.kohan.message.rest.dto.chat

import com.kohan.shared.collection.message.item.ReactionType

class ReactionDto(
    var senderUser: String,
    var type: ReactionType,
) {
    companion object {
        fun from(reaction: com.kohan.shared.collection.message.item.Reaction): ReactionDto =
            ReactionDto(
                senderUser = reaction.senderUser.toString(),
                type = reaction.type,
            )
    }
}
