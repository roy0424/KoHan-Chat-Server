package com.kohan.message.rest.vo.chat.room

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

class InitMessage(
    @field:NotNull(message = "Please enter a content.")
    val content: Any,
    @field:NotBlank(message = "Please enter a sender id.")
    val sender: String,
)
