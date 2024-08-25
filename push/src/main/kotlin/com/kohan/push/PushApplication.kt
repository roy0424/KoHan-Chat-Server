package com.kohan.push

import com.kohan.shared.spring.mongo.config.MongoConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@Import(MongoConfig::class)
@SpringBootApplication
class PushApplication

fun main(args: Array<String>) {
    runApplication<PushApplication>(*args)
}
