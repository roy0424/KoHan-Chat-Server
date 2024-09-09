package com.kohan.authentication.repository

import com.kohan.authentication.collection.UserCollection
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface UserRepository : MongoRepository<UserCollection, ObjectId> {
    fun findByEmail(email: String): UserCollection?

    fun existsByEmail(email: String): Boolean

    @Query("{'tokenInfos.token': ?0}")
    fun findByTokenInfosToken(token: String): UserCollection?
}
