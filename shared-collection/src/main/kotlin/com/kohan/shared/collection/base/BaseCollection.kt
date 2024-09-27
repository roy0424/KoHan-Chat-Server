package com.kohan.shared.collection.base

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.mongodb.core.index.Indexed
import java.time.LocalDateTime

abstract class BaseCollection(
    @Id
    var _id: ObjectId = ObjectId(),
    @CreatedDate
    var createAt: LocalDateTime? = null,
    @LastModifiedDate
    var updateAt: LocalDateTime? = null,
    @Indexed
    var deleteAt: LocalDateTime? = null,
) : Persistable<ObjectId> {
    fun delete() {
        deleteAt = LocalDateTime.now()
    }
    // getId nullable ?
    override fun getId(): ObjectId? = _id

    override fun isNew(): Boolean = createAt == null
}
