package com.kohan.shared.spring.mongo.config

import de.flapdoodle.embed.mongo.commands.MongodArguments
import de.flapdoodle.embed.mongo.config.Storage
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoTransactionManager
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableMongoAuditing
@EnableTransactionManagement
class MongoConfig {
    @Bean
    fun transactionManager(mongoTemplate: MongoTemplate): MongoTransactionManager =
        MongoTransactionManager(mongoTemplate.mongoDatabaseFactory)

    @Bean
    fun mongodArguments(): MongodArguments =
        MongodArguments
            .builder()
            .replication(Storage.of("test", 10))
            .build()
}
