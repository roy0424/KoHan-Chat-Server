package com.kohan.message.rest.vo.friend

import com.kohan.shared.collection.friend.FriendStatus
import com.kohan.shared.spring.validator.ValidEnum
import jakarta.validation.constraints.NotBlank

class FriendStatusVo(
    @field:NotBlank(message = "Please enter a id")
    val id: String,
    @field:ValidEnum(
        message = "Invalid friend status. This is not permitted.",
        enumClass = FriendStatus::class,
    )
    val status: String,
)
