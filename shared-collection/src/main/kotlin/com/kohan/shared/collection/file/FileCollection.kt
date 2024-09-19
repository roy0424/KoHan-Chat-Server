package com.kohan.shared.collection.file

import com.kohan.proto.file.v1.UploadFile
import com.kohan.proto.file.v1.UploadFile.UploadFileInfo
import com.kohan.shared.collection.base.BaseCollection
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
    var uploadChatRoomId: ObjectId? = null,
    /** upload user */
    @Indexed
    var uploadUserId: ObjectId,
) : BaseCollection() {
    companion object {
        fun from(uploadFileInfo: UploadFileInfo): FileCollection =
            FileCollection(
                fileName = UUID.randomUUID().toString(),
                originalFileName = uploadFileInfo.fileName,
                extension = uploadFileInfo.extension,
                fileSize = uploadFileInfo.totalSize,
                uploadChatRoomId = ObjectId(uploadFileInfo.roomId),
                uploadUserId = ObjectId(uploadFileInfo.userId),
            )

        fun from(
            uploadProfileVO: UploadFile.UploadProfile,
            fileExtension: String,
        ): FileCollection =
            FileCollection(
                fileName = UUID.randomUUID().toString(),
                originalFileName = uploadProfileVO.userId + LocalDateTime.now(),
                extension = fileExtension,
                fileSize = uploadProfileVO.fileContent.size().toLong(),
                uploadUserId = ObjectId(uploadProfileVO.userId),
            )
    }
}
