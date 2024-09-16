package com.kohan.file.service.grpc

import com.google.protobuf.ByteString
import com.kohan.file.collection.file.FileCollection
import com.kohan.file.repository.FileRepository
import com.kohan.file.util.FileUtil
import com.kohan.shared.armeria.file.v1.FileUploadServiceGrpcKt
import com.kohan.shared.armeria.file.v1.UploadFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.FileOutputStream
import javax.imageio.ImageIO

@Service
class UploadFileGrpcService(
    @Value("\${kohan.file.profileExtensions}")
    private val profileExtension: String,
    private val fileUtil: FileUtil,
    private val fileRepository: FileRepository,
) : FileUploadServiceGrpcKt.FileUploadServiceCoroutineImplBase() {
    override suspend fun upload(request: UploadFile.UploadFileVO): UploadFile.UploadFileDTO {
        val fileCollection = FileCollection.to(request.info)

        CoroutineScope(Dispatchers.IO).launch {
            val newFile = fileUtil.newFile(fileCollection.fileName)

            newFile.outputStream().buffered().use { stream ->
                fileUtil.writeToFile(
                    request.fileContent.toByteArray(),
                    stream,
                )
            }
        }

        val saved =
            withContext(Dispatchers.IO) {
                fileRepository.save(fileCollection)
            }

        return UploadFile.UploadFileDTO
            .newBuilder()
            .setFileKey(saved._id.toHexString())
            .build()
    }

    override suspend fun uploadProfile(request: UploadFile.UploadProfileVO): UploadFile.UploadFileDTO {
        val fileCollection = FileCollection.to(request, profileExtension)

        CoroutineScope(Dispatchers.IO).launch {
            val image = request.fileContent.newInput().use(ImageIO::read)
            fileUtil.saveCompressedImage(fileCollection.fileName, image)
        }

        val saved =
            withContext(Dispatchers.IO) {
                fileRepository.save(fileCollection)
            }

        return UploadFile.UploadFileDTO
            .newBuilder()
            .setFileKey(saved._id.toHexString())
            .build()
    }

    inner class UploadingFile(
        private val fileCollection: FileCollection,
        private val suffixes: String = "",
    ) {
        val newFile = fileUtil.newFile(fileCollection.fileName + suffixes)

        private var uploadedSize: Long = 0

        private val totalSize = fileCollection.fileSize
        private val fileOutputStream = FileOutputStream(newFile).buffered()

        private val progressDTO = {
            UploadFile.UploadFileDTO
                .newBuilder()
                .setReceived(uploadedSize)
                .setTotal(totalSize)
                .build()
        }

        suspend fun saveChunk(byteString: ByteString): UploadFile.UploadFileDTO {
            if (uploadedSize < totalSize) {
                uploadedSize += byteString.size()
                withContext(Dispatchers.IO) {
                    fileOutputStream.write(byteString.toByteArray())
                }
                return progressDTO()
            }
            close()
            newFile.delete()
            throw IllegalStateException("A larger file was sent than expected.")
        }

        suspend fun done(): UploadFile.UploadFileDTO {
            close()
            return UploadFile.UploadFileDTO
                .newBuilder()
                .setFileKey(saveCollection()._id.toHexString())
                .build()
        }

        private suspend fun saveCollection(): FileCollection =
            withContext(Dispatchers.IO) {
                fileRepository.save(fileCollection)
            }

        private suspend fun close() {
            withContext(Dispatchers.IO) {
                fileOutputStream.close()
            }
        }
    }

    override fun uploadLageFile(requests: Flow<UploadFile.UploadLageFileVO>): Flow<UploadFile.UploadFileDTO> =
        flow {
            var uploadingFile: UploadingFile? = null

            try {
                requests.collect { request ->
                    when {
                        request.hasInfo() -> {
                            uploadingFile = UploadingFile(FileCollection.to(request.info))
                        }
                        request.hasChunk() && uploadingFile != null -> {
                            emit(uploadingFile!!.saveChunk(request.chunk))
                        }
                    }
                }
            } catch (e: IllegalStateException) {
                emit(
                    UploadFile.UploadFileDTO
                        .newBuilder()
                        .setMessage(e.message)
                        .build(),
                )
            }

            if (uploadingFile != null && uploadingFile!!.newFile.exists()) {
                emit(uploadingFile!!.done())
            }
        }

    override fun uploadLageImage(requests: Flow<UploadFile.UploadLageFileVO>): Flow<UploadFile.UploadFileDTO> =
        flow {
            var uploadingFile: UploadingFile? = null

            try {
                requests.collect { request ->
                    when {
                        request.hasInfo() -> {
                            uploadingFile = UploadingFile(FileCollection.to(request.info), ".tmp")
                        }

                        request.hasChunk() && uploadingFile != null -> {
                            emit(uploadingFile!!.saveChunk(request.chunk))
                        }
                    }
                }
            } catch (e: IllegalStateException) {
                emit(
                    UploadFile.UploadFileDTO
                        .newBuilder()
                        .setMessage(e.message)
                        .build(),
                )
            }

            if (uploadingFile != null && uploadingFile!!.newFile.exists()) {
                val image = uploadingFile!!.newFile.inputStream().use(ImageIO::read)
                fileUtil.saveCompressedImage(uploadingFile!!.newFile.name.split(".")[0], image)

                val doneDTO = uploadingFile!!.done()
                uploadingFile!!.newFile.delete()

                emit(doneDTO)
            }
        }
}
