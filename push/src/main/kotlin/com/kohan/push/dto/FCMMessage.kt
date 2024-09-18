package com.kohan.push.dto

class FCMMessage(
    val validateOnly: Boolean,
    val message: Message,
)

class Message(
    val token: String? = null,
    val topic: String? = null,
    val notification: Notification?,
    val data: Map<String, String>,
)

class Notification(
    val title: String,
    val body: String,
    val image: String?,
)
