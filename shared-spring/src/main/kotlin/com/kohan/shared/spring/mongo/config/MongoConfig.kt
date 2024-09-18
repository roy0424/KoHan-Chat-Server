package com.kohan.shared.spring.mongo.config

import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.MongoTransactionManager
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableMongoAuditing
@EnableTransactionManagement
@EnableAutoConfiguration(exclude = [EmbeddedMongoAutoConfiguration::class])
@Profile("prod")
class MongoConfig {
    @Bean
    fun transactionManager(mongoTemplate: MongoTemplate): MongoTransactionManager =
        MongoTransactionManager(mongoTemplate.mongoDatabaseFactory)
}
