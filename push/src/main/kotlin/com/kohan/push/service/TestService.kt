package com.kohan.push.service

import com.kohan.push.firebase.FCMService
import com.kohan.push.vo.SendNotificationVO
import com.linecorp.armeria.server.annotation.Post
import org.springframework.stereotype.Service

@Service
class TestService(
    private val fcmService: FCMService,
) {
    @Post("/send-notification-to-token")
    fun sendNotificationToToken(data: SendNotificationVO) {
        if (data.token == null) {
            throw IllegalArgumentException("Token must not be null")
        }
        fcmService.sendNotificationToToken(
            token = data.token,
            title = data.title,
            body = data.body,
            image = data.image,
            chatRoomId = data.chatRoomId,
        )
    }

    @Post("/send-notification-to-topic")
    fun sendNotificationToTopic(data: SendNotificationVO) {
        if (data.topic == null) {
            throw IllegalArgumentException("Topic must not be null")
        }
        fcmService.sendNotificationToTopic(
            topic = data.topic,
            title = data.title,
            body = data.body,
            image = data.image,
            chatRoomId = data.chatRoomId,
        )
    }
}
