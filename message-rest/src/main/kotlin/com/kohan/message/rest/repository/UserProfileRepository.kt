package com.kohan.message.rest.repository

import com.kohan.shared.collection.user.UserProfileCollection
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface UserProfileRepository: MongoRepository<UserProfileCollection, ObjectId> {
    fun save(userProfileCollection: UserProfileCollection): UserProfileCollection

    fun findByUserId(userId: ObjectId): UserProfileCollection?
}