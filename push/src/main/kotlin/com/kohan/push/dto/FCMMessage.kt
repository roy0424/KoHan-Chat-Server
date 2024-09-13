package com.kohan.push.dto

data class FCMMessage(
    val validateOnly: Boolean,
    val message: Message,
)

data class Message(
    val token: String? = null,
    val topic: String? = null,
    val condition: String? = null,
    val notification: Notification?,
    val data: Map<String, String>,
)

data class Notification(
    val title: String,
    val body: String,
    val image: String?,
)
