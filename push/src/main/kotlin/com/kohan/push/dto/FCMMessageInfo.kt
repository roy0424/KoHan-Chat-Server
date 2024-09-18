package com.kohan.push.dto

class FCMMessageInfo(
    val token: String? = null,
    val topic: String? = null,
    val title: String,
    val body: String,
    val image: String?,
    val chatRoomId: String,
)
