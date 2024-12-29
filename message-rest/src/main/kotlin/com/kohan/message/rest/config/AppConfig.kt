package com.kohan.message.rest.config

import com.kohan.message.rest.service.annotation.ChatService
import com.kohan.message.rest.service.annotation.FriendService
import com.kohan.message.rest.service.annotation.UserProfileService
import com.kohan.message.rest.service.grpc.UserProfileGrpcService
import com.linecorp.armeria.server.HttpService
import com.linecorp.armeria.server.docs.DocService
import com.linecorp.armeria.server.grpc.GrpcService
import com.linecorp.armeria.server.logging.AccessLogWriter
import com.linecorp.armeria.server.logging.LoggingService
import com.linecorp.armeria.spring.ArmeriaServerConfigurator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {
    @Bean
    fun armeriaServerConfigurator(
        userProfileService: UserProfileService,
        userProfileGrpcService: UserProfileGrpcService,
        chatService: ChatService,
        friendService: FriendService
    ): ArmeriaServerConfigurator =
        ArmeriaServerConfigurator { serverBuilder ->
            serverBuilder.annotatedService("/api/user-profile", userProfileService).decorator { delegate ->
                TokenValidationService(delegate)
            }
            serverBuilder.annotatedService("/api/chat", chatService).decorator { delegate ->
                TokenValidationService(delegate)
            }
            serverBuilder.annotatedService("/api/friend", friendService).decorator { delegate ->
                TokenValidationService(delegate)
            }
            serverBuilder.service(
                "prefix:/grpc/v1",
                GrpcService
                    .builder()
                    .addService(userProfileGrpcService)
                    .enableUnframedRequests(true)
                    .build(),
            )

            serverBuilder.serviceUnder("/docs", DocService())
            serverBuilder.decorator(LoggingService.newDecorator())
            serverBuilder.accessLogWriter(AccessLogWriter.combined(), false)
        }
}
