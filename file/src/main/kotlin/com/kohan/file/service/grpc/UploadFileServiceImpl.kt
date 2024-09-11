package com.kohan.file.service.grpc

import com.kohan.file.repository.FileRepository
import com.kohan.file.util.FileUtil
import com.kohan.shared.armeria.client.grpc.authentication.AuthenticationClient
import com.kohan.shared.armeria.file.v1.FileUploadServiceGrpcKt.FileUploadServiceCoroutineImplBase
import com.kohan.shared.armeria.file.v1.UploadFile.UploadFileVO
import com.kohan.shared.armeria.file.v1.UploadFile.UploadFileDTO
import com.kohan.shared.armeria.file.v1.uploadFileDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class UploadFileServiceImpl(
    private val fileUtil: FileUtil,
    private val fileRepository: FileRepository
) : FileUploadServiceCoroutineImplBase() {
    override suspend fun upload(request: UploadFileVO): UploadFileDTO {
        val fileCollection = fileUtil.toFileCollection(request)

        CoroutineScope(Dispatchers.IO).launch{
            fileUtil.saveByteArrayToFile(
                fileCollection.fileName,
                request.fileContent.toByteArray(),
                )
        }

        val saved = withContext(Dispatchers.IO){
            fileRepository.save(fileCollection)
        }

        return UploadFileDTO.newBuilder().setFileKey(saved._id.toHexString()).build();
    }
}