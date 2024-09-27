package com.kohan.message.rest.vo.user.profile

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

class Nickname(
    @field:Size(min = 2, max = 20, message = "Please enter a nickname of at least 2 characters and no more than 10 characters.")
    @field:NotBlank(message = "Please enter a nickname.")
    val nickname: String,
)