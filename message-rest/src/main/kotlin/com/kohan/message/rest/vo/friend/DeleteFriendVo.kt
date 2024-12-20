package com.kohan.message.rest.vo.friend

import jakarta.validation.constraints.NotBlank

class DeleteFriendVo(
    @field:NotBlank(message = "Please enter a id")
    val id: String,
)
