package com.kohan.file.service.grpc

import com.kohan.file.collection.file.FileCollection
import com.kohan.file.util.FileUtil
import com.kohan.shared.armeria.file.v1.FileUploadServiceGrpcKt
import com.kohan.shared.armeria.file.v1.UploadFile
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class UploadFileGrpcService(
    @Value("\${kohan.file.profileExtensions}")
    private val profileExtension: String,
    private val fileUtil: FileUtil,
) : FileUploadServiceGrpcKt.FileUploadServiceCoroutineImplBase() {
    override suspend fun upload(request: UploadFile.UploadFileVO): UploadFile.UploadFileDTO {
        val fileCollection = FileCollection.to(request)
        val saved = fileUtil.uploadFile(fileCollection, request.fileContent.newInput())
        return UploadFile.UploadFileDTO
            .newBuilder()
            .setFileKey(saved._id.toHexString())
            .build()
    }

    override suspend fun uploadProfile(request: UploadFile.UploadProfileVO): UploadFile.UploadFileDTO {
        val fileCollection = FileCollection.to(request, profileExtension)
        val saved = fileUtil.uploadProfile(fileCollection, request.fileContent.newInput())
        return UploadFile.UploadFileDTO
            .newBuilder()
            .setFileKey(saved._id.toHexString())
            .build()
    }
}
