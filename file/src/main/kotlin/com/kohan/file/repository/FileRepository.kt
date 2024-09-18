package com.kohan.file.repository

import com.kohan.shared.collection.file.FileCollection
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface FileRepository : MongoRepository<FileCollection, ObjectId>
