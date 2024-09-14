package com.kohan.file.collection.file

import com.kohan.shared.armeria.file.v1.UploadFile.UploadFileVO
import com.kohan.shared.armeria.file.v1.UploadFile.UploadProfileVO
import com.kohan.shared.spring.mongo.collection.base.BaseCollection
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.UUID

@Document(collection = "file")
class FileCollection(
    /** Original file name */
    var originalFileName: String,
    /** The name of the file stored on the server file system */
    var fileName: String,
    /** Original file extension */
    var extension: String,
    /** File size Bytes */
    var fileSize: Long,
    /** Upload chat room
     *
     * If null, it's visible to everyone */
    @Indexed
    var uploadChatRoomKey: ObjectId? = null,
    /** upload user */
    @Indexed
    var uploadUserKey: ObjectId,
) : BaseCollection() {
    companion object {
        fun to(uploadFileVO: UploadFileVO): FileCollection =
            FileCollection(
                fileName = UUID.randomUUID().toString(),
                originalFileName = uploadFileVO.fileName,
                extension = uploadFileVO.extension,
                fileSize = uploadFileVO.fileContent.size().toLong(),
                uploadChatRoomKey = ObjectId(uploadFileVO.roomKey),
                uploadUserKey = ObjectId(uploadFileVO.userKey),
            )

        fun to(
            uploadProfileVO: UploadProfileVO,
            fileExtension: String,
        ): FileCollection =
            FileCollection(
                fileName = UUID.randomUUID().toString(),
                originalFileName = uploadProfileVO.userKey + LocalDateTime.now(),
                extension = fileExtension,
                fileSize = uploadProfileVO.fileContent.size().toLong(),
                uploadUserKey = ObjectId(uploadProfileVO.userKey),
            )
    }
}
