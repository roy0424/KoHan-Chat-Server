package com.kohan.file.collection.file

import com.kohan.shared.spring.mongo.collection.base.BaseCollection
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "file")
class FileCollection(
    /** Original file name */
    var originalFileName: String,
    /** The name of the file stored on the server file system */
    var fileName: String,
    /** Original file extension */
    var extension: String,
    /** File size Bytes */
    var fileSize: Int,
    /** Upload chat room */
    @Indexed
    var uploadChatRoomKey: ObjectId,
    /** upload user */
    @Indexed
    var uploadUserKey: ObjectId,
) : BaseCollection()
