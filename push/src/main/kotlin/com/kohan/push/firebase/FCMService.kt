package com.kohan.push.firebase

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.auth.oauth2.GoogleCredentials
import com.kohan.push.dto.FCMMessage
import com.kohan.push.dto.Message
import com.kohan.push.dto.Notification
import com.kohan.push.vo.SendNotification
import com.linecorp.armeria.client.WebClient
import com.linecorp.armeria.common.HttpData
import com.linecorp.armeria.common.HttpHeaderNames
import com.linecorp.armeria.common.HttpMethod
import com.linecorp.armeria.common.HttpRequest
import com.linecorp.armeria.common.MediaType
import com.linecorp.armeria.common.RequestHeaders
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.FileInputStream

@Service
class FCMService(
    @Value("\${kohan.push.firebase.messages.send.url}")
    private val apiUrl: String,
    @Value("\${kohan.push.firebase.secret.key.path}")
    private val firebaseConfigPath: String,
    private val objectMapper: ObjectMapper,
) {
    fun sendNotification(fcmMessageInfo: SendNotification) {
        val fcmMessage =
            makeFCMMessage(fcmMessageInfo)
        val webClient = WebClient.builder(apiUrl).build()
        val headers = createRequestHeaders()
        val request = HttpRequest.of(headers, HttpData.ofUtf8(fcmMessage))

        webClient
            .execute(request)
            .aggregate()
            .handle { response, throwable ->
                if (throwable != null) {
                    println("Error sending notification: ${throwable.message}")
                    return@handle
                }
                val statusCode = response.status().code()
                val responseBody = response.contentUtf8()
                println("Response status: $statusCode")
                println("Response body: $responseBody")
            }
    }

    private fun makeFCMMessage(fcmMessageInfo: SendNotification): String {
        val fcmMessage =
            FCMMessage(
                validateOnly = false,
                message =
                    Message(
                        token = fcmMessageInfo.token,
                        topic = fcmMessageInfo.topic,
                        notification =
                            Notification(
                                title = fcmMessageInfo.title,
                                body = fcmMessageInfo.body,
                                image = fcmMessageInfo.image,
                            ),
                        data =
                            mapOf(
                                "chatRoomId" to fcmMessageInfo.chatRoomId,
                            ),
                    ),
            )
        return objectMapper.writeValueAsString(fcmMessage)
    }

    private fun createRequestHeaders(): RequestHeaders =
        RequestHeaders.of(
            HttpMethod.POST,
            "/",
            HttpHeaderNames.AUTHORIZATION,
            "Bearer ${getAccessToken()}",
            HttpHeaderNames.CONTENT_TYPE,
            MediaType.JSON_UTF_8,
        )

    private fun getAccessToken(): String? {
        val serviceAccount = FileInputStream(firebaseConfigPath)
        val googleCredentials =
            GoogleCredentials
                .fromStream(serviceAccount)
                .createScoped("https://www.googleapis.com/auth/cloud-platform")

        googleCredentials.refreshIfExpired()
        return googleCredentials.refreshAccessToken().tokenValue
    }
}
