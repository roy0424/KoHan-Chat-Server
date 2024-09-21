package com.kohan.message.rest.repository

import com.kohan.shared.collection.user.UserCollection
import com.kohan.shared.collection.user.UserProfileCollection
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface UserProfileRepository: MongoRepository<UserCollection, ObjectId> {
    fun save(userProfileCollection: UserProfileCollection): UserProfileCollection
}