package com.kohan.push.firebase

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.auth.oauth2.GoogleCredentials
import com.kohan.push.dto.FCMMessage
import com.kohan.push.dto.Message
import com.kohan.push.dto.Notification
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
    fun sendNotificationToToken(
        token: String,
        title: String,
        body: String,
        image: String?,
        chatRoomId: String,
    ) {
        val fcmMessage =
            makeFCMMessage(
                token = token,
                title = title,
                body = body,
                image = image,
                data = mapOf("chatRoomId" to chatRoomId),
            )
        val client =
            WebClient
                .builder(apiUrl)
                .build()

        val headers =
            RequestHeaders.of(
                HttpMethod.POST,
                "/",
                HttpHeaderNames.AUTHORIZATION,
                "Bearer ${getAccessToken()}",
                HttpHeaderNames.CONTENT_TYPE,
                MediaType.JSON_UTF_8,
            )

        val request = HttpRequest.of(headers, HttpData.ofUtf8(fcmMessage))

        client
            .execute(request)
            .aggregate()
            .handle { response, throwable ->
                val statusCode = response.status().code()
                val responseBody = response.contentUtf8()
                println("Response status: $statusCode")
                println("Response body: $responseBody")
            }
    }

    fun sendNotificationToTopic(
        topic: String,
        title: String,
        body: String,
        image: String?,
        chatRoomId: String,
    ) {
        val fcmMessage =
            makeFCMMessage(
                topic = topic,
                title = title,
                body = body,
                image = image,
                data = mapOf("chatRoomId" to chatRoomId),
            )
        val client =
            WebClient
                .builder(apiUrl)
                .build()

        val headers =
            RequestHeaders.of(
                HttpMethod.POST,
                "/",
                HttpHeaderNames.AUTHORIZATION,
                "Bearer ${getAccessToken()}",
                HttpHeaderNames.CONTENT_TYPE,
                MediaType.JSON_UTF_8,
            )

        val request = HttpRequest.of(headers, HttpData.ofUtf8(fcmMessage))

        client
            .execute(request)
            .aggregate()
            .handle { response, throwable ->
                val statusCode = response.status().code()
                val responseBody = response.contentUtf8()
                println("Response status: $statusCode")
                println("Response body: $responseBody")
            }
    }

    private fun makeFCMMessage(
        token: String? = null,
        topic: String? = null,
        condition: String? = null,
        title: String,
        body: String,
        image: String?,
        data: Map<String, String>,
    ): String {
        val fcmMessage =
            FCMMessage(
                validateOnly = false,
                message =
                    Message(
                        token = token,
                        topic = topic,
                        condition = condition,
                        notification =
                            Notification(
                                title = title,
                                body = body,
                                image = image,
                            ),
                        data = data,
                    ),
            )
        return objectMapper.writeValueAsString(fcmMessage)
    }

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
