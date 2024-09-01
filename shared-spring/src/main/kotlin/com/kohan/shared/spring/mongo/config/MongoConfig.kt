package com.kohan.shared.spring.mongo.config

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoTransactionManager
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableMongoAuditing
@EnableTransactionManagement
class MongoConfig(
    @Value("\${spring.data.mongodb.uri}")
    private val databaseUrl: String,
    @Value("\${spring.data.mongodb.database}")
    private val databaseName: String,
) {
    @Bean
    fun mongoClient(): MongoClient {
        return MongoClients.create(databaseUrl)
    }

    @Bean
    fun mongoTemplate(mongoClient: MongoClient): MongoTemplate {
        return MongoTemplate(mongoClient, databaseName)
    }

    @Bean
    fun transactionManager(mongoTemplate: MongoTemplate): MongoTransactionManager {
        return MongoTransactionManager(mongoTemplate.mongoDatabaseFactory)
    }
}
