package com.kohan.file.service.grpc

import com.google.protobuf.ByteString
import com.kohan.file.repository.FileRepository
import com.kohan.file.util.FileUtil
import com.kohan.proto.file.v1.FileUploadServiceGrpcKt
import com.kohan.proto.file.v1.UploadFile
import com.kohan.shared.collection.file.FileCollection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.FileOutputStream
import java.util.concurrent.CancellationException
import javax.imageio.ImageIO

@Service
class UploadFileGrpcService(
    @Value("\${kohan.file.profileExtensions}")
    private val profileExtension: String,
    @Value("\${kohan.file.maxUploadFileSize}")
    private val maxUploadFileSize: Long,
    private val fileUtil: FileUtil,
    private val fileRepository: FileRepository,
) : FileUploadServiceGrpcKt.FileUploadServiceCoroutineImplBase() {
    override suspend fun uploadProfile(request: UploadFile.UploadProfile): UploadFile.UploadFileDTO {
        val fileCollection = FileCollection.of(request, profileExtension)

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
        private val suffixes: String = "",
    ) {
        private var uploadedSize: Long = 0

        private lateinit var fileCollection: FileCollection

        val newFile by lazy {
            fileUtil.newFile(fileCollection.fileName + suffixes)
        }

        private val totalSize by lazy {
            fileCollection.fileSize
        }

        private val fileOutputStream by lazy {
            FileOutputStream(newFile).buffered()
        }

        private val progressDTO = {
            UploadFile.UploadFileDTO
                .newBuilder()
                .setReceived(uploadedSize)
                .setTotal(totalSize)
                .build()
        }

        fun isInit(): Boolean = ::fileCollection.isInitialized

        fun init(fileCollection: FileCollection) {
            this.fileCollection = fileCollection
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

    override fun uploadLargeFile(requests: Flow<UploadFile.UploadLargeFile>): Flow<UploadFile.UploadFileDTO> =
        flow {
            val uploadingFile = UploadingFile()

            saveFileFromStreamFlow(requests, uploadingFile)

            if (uploadingFile.isInit() && uploadingFile.newFile.exists()) {
                emit(uploadingFile.done())
            }
        }

    override fun uploadLargeImage(requests: Flow<UploadFile.UploadLargeFile>): Flow<UploadFile.UploadFileDTO> =
        flow {
            val uploadingFile = UploadingFile(".tmp")

            saveFileFromStreamFlow(requests, uploadingFile)

            if (uploadingFile.isInit() && uploadingFile.newFile.exists()) {
                val image = uploadingFile.newFile.inputStream().use(ImageIO::read)
                fileUtil.saveCompressedImage(uploadingFile.newFile.name.split(".")[0], image)

                val doneDTO = uploadingFile.done()
                uploadingFile.newFile.delete()

                emit(doneDTO)
            }
        }

    private suspend fun FlowCollector<UploadFile.UploadFileDTO>.saveFileFromStreamFlow(
        requests: Flow<UploadFile.UploadLargeFile>,
        uploadingFile: UploadingFile,
    ) {
        requests.cancellable().collect { request ->
            try {
                when {
                    request.hasInfo() -> {
                        checkUploadInfo(request.info)
                        uploadingFile.init(FileCollection.of(request.info))
                    }

                    request.hasChunk() && uploadingFile.isInit() -> {
                        emit(uploadingFile.saveChunk(request.chunk))
                    }

                    request.hasChunk() && !uploadingFile.isInit() -> {
                        throw IllegalStateException("Before upload a file, you must send file info.")
                    }
                }
            } catch (e: IllegalStateException) {
                currentCoroutineContext().cancel(CancellationException(e.message))
            }
        }
    }

    private fun checkUploadInfo(request: UploadFile.UploadFileInfo) {
        // todo: Validation
        if (request.totalSize > maxUploadFileSize) {
            throw IllegalStateException("The maximum upload size is $maxUploadFileSize Byte")
        }
    }
}
