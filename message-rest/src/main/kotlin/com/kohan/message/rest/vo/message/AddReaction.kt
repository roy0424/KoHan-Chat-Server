package com.kohan.message.rest.vo.message

import com.kohan.shared.enum.message.item.ReactionType
import com.kohan.shared.spring.validator.ValidEnum
import jakarta.validation.constraints.NotBlank

data class AddReaction(
    @field:NotBlank(message = "Please enter a message id.")
    val messageId: String,
    @field:ValidEnum(
        message = "Invalid reaction type. This is not permitted.",
        enumClass = ReactionType::class,
    )
    val type: String,
    @field:NotBlank(message = "Please enter a sender id.")
    val sender: String,
)
