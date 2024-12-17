package com.kohan.message.rest.vo.message

import jakarta.validation.constraints.NotBlank

class DeleteMessage(
    @field:NotBlank(message = "Please enter a message id.")
    val messageId: String,
)
