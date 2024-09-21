package com.kohan.authentication

import com.kohan.shared.spring.mongo.config.EmbeddedMongoConfig
import com.kohan.shared.spring.mongo.config.MongoConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@Import(MongoConfig::class, EmbeddedMongoConfig::class)
@SpringBootApplication
class AuthenticationApplication

fun main(args: Array<String>) {
    runApplication<AuthenticationApplication>(*args)
}
