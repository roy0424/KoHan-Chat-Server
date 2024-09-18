package com.kohan.push.config

import com.kohan.push.service.TestService
import com.kohan.push.service.grpc.FCMTokenGrpcService
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
        testService: TestService,
        fcmTokenGrpcService: FCMTokenGrpcService,
    ): ArmeriaServerConfigurator =
        ArmeriaServerConfigurator { serverBuilder ->
            serverBuilder.annotatedService("/test", testService)
            serverBuilder.service(
                "prefix:/grpc/v1",
                GrpcService
                    .builder()
                    .addService(fcmTokenGrpcService)
                    .enableUnframedRequests(true)
                    .build(),
            )
            serverBuilder.serviceUnder("/docs", DocService())
            serverBuilder.decorator(LoggingService.newDecorator())
            serverBuilder.accessLogWriter(AccessLogWriter.combined(), false)
        }
}
