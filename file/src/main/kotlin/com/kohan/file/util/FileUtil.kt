package com.kohan.file.util

import com.kohan.file.collection.file.FileCollection
import com.kohan.shared.armeria.authentication.v1.Authentication.UserDto
import com.kohan.shared.armeria.file.v1.UploadFile.UploadFileVO
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.UUID
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component


@Component
class FileUtil(
    @Value("\${kohan.file.savePath}")
    private val savePath: String
) {
    fun toFileCollection(uploadFileVO: UploadFileVO): FileCollection{
        return FileCollection(
            fileName = UUID.randomUUID().toString(),
            originalFileName = uploadFileVO.fileName,
            extension = uploadFileVO.extension,
            fileSize = uploadFileVO.fileContent.size(),
            uploadChatRoomKey = ObjectId(uploadFileVO.roomKey),
            uploadUserKey = ObjectId(uploadFileVO.userKey),
        )
    }

    fun saveByteArrayToFile(fileName: String, fileContent: ByteArray){
        val newFile = File(savePath, fileName)
        FileOutputStream(newFile).use{
            it.write(fileContent)
        }
    }
}