package com.kohan.shared.spring.config

import com.kohan.proto.push.v1.FCMTokenServiceGrpc.FCMTokenServiceBlockingStub
import com.linecorp.armeria.client.grpc.GrpcClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PushGrpcClient(
    @Value("\${kohan.push.port}")
    private val port: Int,
) {
    @Bean
    fun initPushGrpcClient(): FCMTokenServiceBlockingStub =
        GrpcClients.newClient(
            "gproto+http://localhost:$port/grpc/v1/",
            FCMTokenServiceBlockingStub::class.java,
        )
}
