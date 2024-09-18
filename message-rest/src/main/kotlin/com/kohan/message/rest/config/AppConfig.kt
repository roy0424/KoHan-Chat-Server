package com.kohan.message.rest.config

import com.linecorp.armeria.server.docs.DocService
import com.linecorp.armeria.server.logging.AccessLogWriter
import com.linecorp.armeria.server.logging.LoggingService
import com.linecorp.armeria.spring.ArmeriaServerConfigurator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class AppConfig {
    @Configuration
    class AppConfig {
        @Bean
        fun armeriaServerConfigurator(): ArmeriaServerConfigurator =
            ArmeriaServerConfigurator { serverBuilder ->

                serverBuilder.serviceUnder("/docs", DocService())
                serverBuilder.decorator(LoggingService.newDecorator())
                serverBuilder.accessLogWriter(AccessLogWriter.combined(), false)
            }
    }
}
