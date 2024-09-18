package com.kohan.push.service

import com.kohan.push.firebase.FCMService
import com.kohan.push.vo.SendNotification
import com.linecorp.armeria.server.annotation.Post
import org.springframework.stereotype.Service

@Service
class TestService(
    private val fcmService: FCMService,
) {
    @Post("/send-notification-to-token")
    fun sendNotificationToToken(data: SendNotification) {
        if (data.token == null) {
            throw IllegalArgumentException("Token must not be null")
        }
        fcmService.sendNotification(data)
    }

    @Post("/send-notification-to-topic")
    fun sendNotificationToTopic(data: SendNotification) {
        if (data.topic == null) {
            throw IllegalArgumentException("Topic must not be null")
        }
        fcmService.sendNotification(data)
    }
}
