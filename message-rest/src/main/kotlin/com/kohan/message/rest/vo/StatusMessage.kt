package com.kohan.message.rest.vo

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

class StatusMessage(
    @field:Size(min = 0, max = 20, message = "Please enter a nickname of at least 2 characters and no more than 10 characters.")
    @field:NotNull(message = "Please enter a status message.")
    val statusMessage: String,
)
