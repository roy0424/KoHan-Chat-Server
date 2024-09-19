package com.kohan.file.service.grpc

import com.google.protobuf.ByteString
import com.kohan.file.repository.FileRepository
import com.kohan.file.util.FileUtil
import com.kohan.proto.file.v1.DownloadFile
import com.kohan.proto.file.v1.FileDownloadServiceGrpcKt
import com.kohan.shared.collection.file.FileCollection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.util.concurrent.CancellationException

@Service
class DownloadFileGrpcService(
    private val fileUtil: FileUtil,
    private val fileRepository: FileRepository,
) : FileDownloadServiceGrpcKt.FileDownloadServiceCoroutineImplBase() {
    override fun downloadFile(request: DownloadFile.FileDownloadRequest): Flow<DownloadFile.FileDownloadResponse> =
        flow {
            try {
                val fileCollection =
                    withContext(Dispatchers.IO) {
                        val optional = fileRepository.findById(ObjectId(request.fileId))
                        if (optional.isEmpty) {
                            throw IllegalStateException("Unregistered File Id")
                        }
                        optional.get()
                    }

                emit(createInitialResponse(fileCollection))

                val file = fileUtil.newFile(fileCollection.fileName)
                file.inputStream().buffered().use { inputStream ->
                    val buffer = ByteArray(request.chunkSize)
                    var bytesRead: Int
                    var sendData: Long = 0

                    while (inputStream.read(buffer).also {
                            bytesRead = it
                            sendData += it
                        } != -1
                    ) {
                        val chunk = if (bytesRead == request.chunkSize) buffer else buffer.copyOf(bytesRead)
                        emit(createChunkResponse(chunk, sendData))
                    }
                }
            } catch (e: IllegalStateException) {
                currentCoroutineContext().cancel(CancellationException(e.message))
            }
        }

    private fun createInitialResponse(fileCollection: FileCollection): DownloadFile.FileDownloadResponse {
        val uploadChatRoomId = fileCollection.uploadChatRoomId

        return DownloadFile.FileDownloadResponse
            .newBuilder()
            .setInfo(
                DownloadFile.DownloadFileInfo
                    .newBuilder()
                    .setFileName(fileCollection.originalFileName)
                    .setExtension(fileCollection.extension)
                    .setTotalSize(fileCollection.fileSize)
                    .setRoomId(if (uploadChatRoomId != null) uploadChatRoomId.toHexString() else "")
                    .setUserId(fileCollection.uploadUserId.toHexString()),
            ).build()
    }

    private fun createChunkResponse(
        chunk: ByteArray,
        sendData: Long,
    ): DownloadFile.FileDownloadResponse =
        DownloadFile.FileDownloadResponse
            .newBuilder()
            .setFileData(
                DownloadFile.FileData
                    .newBuilder()
                    .setChunk(ByteString.copyFrom(chunk))
                    .setSendByte(sendData)
                    .build(),
            ).build()
}
