package com.kohan.message.rest

import com.kohan.shared.spring.mongo.config.MongoConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@Import(MongoConfig::class)
@SpringBootApplication
class MessageRestApplication

fun main(args: Array<String>) {
    runApplication<MessageRestApplication>(*args)
}
