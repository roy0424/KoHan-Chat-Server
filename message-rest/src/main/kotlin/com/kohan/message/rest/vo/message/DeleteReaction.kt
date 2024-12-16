package com.kohan.message.rest.vo.message

import jakarta.validation.constraints.NotBlank

class DeleteReaction(
    @field:NotBlank(message = "Please enter a message id.")
    val messageId: String,
    @field:NotBlank(message = "Please enter a sender id.")
    val sender: String,
)
