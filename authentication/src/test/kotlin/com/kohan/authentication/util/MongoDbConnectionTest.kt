package com.kohan.authentication.util

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate

@SpringBootTest
class MongoDbConnectionTest {
    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    @Test
    fun testMongoDbConnection() {
        // MongoDB 연결 상태 확인
        val collectionNames = mongoTemplate.db.listCollectionNames().toList()
        assertTrue(collectionNames.isNotEmpty(), "MongoDB에 연결할 수 없습니다.")
    }
}
