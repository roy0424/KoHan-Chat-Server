package com.kohan.file.util

import com.google.protobuf.ByteString
import com.kohan.file.repository.FileRepository
import com.kohan.proto.file.v1.DownloadFile
import com.kohan.proto.file.v1.FileDownloadServiceGrpcKt
import com.kohan.proto.file.v1.FileUploadServiceGrpcKt
import com.kohan.proto.file.v1.UploadFile
import com.linecorp.armeria.client.grpc.GrpcClients
import com.linecorp.armeria.common.grpc.GrpcSerializationFormats
import io.grpc.StatusException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.test.annotation.DirtiesContext
import java.io.File
import java.io.FileInputStream
import java.time.Duration
import java.util.UUID
import javax.imageio.ImageIO
import kotlin.test.assertEquals

@SpringBootTest
@DirtiesContext
@AutoConfigureDataMongo
@EnableAutoConfiguration
@DisabledOnOs(OS.LINUX, disabledReason = "Not working with github action")
class FileServerTest
    @Autowired
    constructor(
        private val fileUtil: FileUtil,
        private val fileRepository: FileRepository,
    ) {
        @Test
        fun saveCompressedImage() =
            runTest {
                val testFile = ClassPathResource("DummyFiles/test.jpg").file
                val fileStream = testFile.inputStream()
                val image = fileStream.use(ImageIO::read)

                val newFileName = UUID.randomUUID().toString()
                fileUtil.saveCompressedImage(newFileName, image)

                val savedFile = fileUtil.newFile(newFileName)
                assertTrue(
                    testFile.length() > savedFile.length(),
                )
            }

        @Test
        fun uploadLageFileTest() =
            runTest {
                val testFile = ClassPathResource("DummyFiles/22mb.txt").file
                val initialUploadVO = createInitialUploadVO(testFile)
                val savedFileId = uploadFile(testFile, false, initialUploadVO)

                assertTrue(savedFileId != "")

                val fileCollection = fileRepository.findById(ObjectId(savedFileId))
                assertTrue(fileCollection.isPresent)

                val savedFile = fileUtil.newFile(fileCollection.get().fileName)
                assertTrue(savedFile.exists())
                assertTrue(savedFile.length() >= testFile.length())
            }

        @Test
        fun uploadLageImageTest() =
            runTest {
                val testFile = ClassPathResource("DummyFiles/22mb.jpg").file
                val initialUploadVO = createInitialUploadVO(testFile)
                val savedFileId = uploadFile(testFile, true, initialUploadVO)

                assertTrue(savedFileId != "")

                val fileCollection = fileRepository.findById(ObjectId(savedFileId))
                assertTrue(fileCollection.isPresent)

                val savedFile = fileUtil.newFile(fileCollection.get().fileName)
                assertTrue(savedFile.exists())
                assertTrue(savedFile.length() < testFile.length())
            }

        @Test
        fun totalSizeExceededText() =
            runTest {
                val testFile = ClassPathResource("DummyFiles/22mb.jpg").file
                val initialUploadVO =
                    UploadFile.UploadLargeFile
                        .newBuilder()
                        .setInfo(
                            UploadFile.UploadFileInfo
                                .newBuilder()
                                .setFileName(testFile.name)
                                .setExtension(testFile.extension)
                                .setTotalSize(testFile.length() - (1024 * 1024 * 32))
                                .setRoomId(ObjectId.get().toHexString())
                                .setUserId(ObjectId.get().toHexString()),
                        ).build()

                assertThrows<StatusException> {
                    uploadFile(testFile, false, initialUploadVO)
                }
            }

        @Test
        fun notSendingFileInfoTest() =
            runTest {
                val testFile = ClassPathResource("/DummyFiles/22mb.jpg").file

                assertThrows<StatusException> {
                    val client = createGrpcClient<FileUploadServiceGrpcKt.FileUploadServiceCoroutineStub>()
                    val request = createFileUploadFlow(testFile, null)
                    val responses = client.uploadLargeFile(request)
                    handleUploadResponses(responses)
                }
            }

        @Test
        fun downloadTest() =
            runTest {
                val testFile = ClassPathResource("DummyFiles/test.txt").file
                val initialUploadVO = createInitialUploadVO(testFile)
                val savedFileId = uploadFile(testFile, false, initialUploadVO)

                val request =
                    DownloadFile.FileDownloadRequest
                        .newBuilder()
                        .setFileId(savedFileId)
                        .setChunkSize(1024)
                        .build()

                val client = createGrpcClient<FileDownloadServiceGrpcKt.FileDownloadServiceCoroutineStub>()
                val downloadFile = fileUtil.newFile(testFile.name + "_down")
                lateinit var fileInfo: DownloadFile.DownloadFileInfo
                client.downloadFile(request).collect { response ->
                    downloadFile.outputStream().buffered().use { outputStream ->
                        when {
                            response.hasInfo() -> {
                                fileInfo = response.info

                                assertEquals(testFile.name, response.info.fileName)
                                assertEquals(testFile.extension, response.info.extension)
                                assertEquals(testFile.length(), response.info.totalSize)
                            }

                            response.hasFileData() -> {
                                println("${response.fileData.sendByte} / ${fileInfo.totalSize}")
                                outputStream.write(response.fileData.chunk.toByteArray())
                            }
                        }
                    }
                }
                assertEquals(
                    testFile.readBytes().toString(Charsets.UTF_8),
                    downloadFile.readBytes().toString(Charsets.UTF_8),
                )
            }

        private suspend fun uploadFile(
            file: File,
            imageCompressing: Boolean,
            initialUploadVO: UploadFile.UploadLargeFile,
        ): String {
            val client = createGrpcClient<FileUploadServiceGrpcKt.FileUploadServiceCoroutineStub>()
            val request = createFileUploadFlow(file, initialUploadVO)
            val responses =
                if (imageCompressing) {
                    client.uploadLargeImage(request)
                } else {
                    client.uploadLargeFile(request)
                }
            return handleUploadResponses(responses)
        }

        private fun createFileUploadFlow(
            file: File,
            initialUploadVO: UploadFile.UploadLargeFile?,
        ): Flow<UploadFile.UploadLargeFile> =
            flow {
                val chunkSize: Int = 1024 * 1024 * 16

                if (initialUploadVO != null) {
                    emit(initialUploadVO)
                }

                withContext(Dispatchers.IO) {
                    FileInputStream(file).buffered()
                }.use { inputStream ->
                    val buffer = ByteArray(chunkSize)
                    var bytesRead: Int

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        val chunk = if (bytesRead == chunkSize) buffer else buffer.copyOf(bytesRead)
                        emit(createChunkUploadVO(chunk))
                    }
                }
            }

        private suspend fun handleUploadResponses(responses: Flow<UploadFile.UploadFileDTO>): String {
            var savedFileId = ""
            responses.collect { response ->
                when {
                    response.hasReceived() and response.hasTotal() -> {
                        println("${response.received} / ${response.total}")
                    }

                    response.hasFileId() -> {
                        savedFileId = response.fileId
                    }

                    response.hasMessage() -> {
                        throw IllegalStateException(response.message)
                    }
                }
            }
            return savedFileId
        }

        private inline fun <reified T : Any> createGrpcClient(): T =
            GrpcClients
                .builder("gproto+http://127.0.0.1:8080/grpc/v1/")
                .serializationFormat(GrpcSerializationFormats.PROTO)
                .responseTimeout(Duration.ZERO)
                .writeTimeout(Duration.ZERO)
                .maxRequestMessageLength(-1)
                .maxResponseMessageLength(-1)
                .build(T::class.java)

        private fun createInitialUploadVO(file: File): UploadFile.UploadLargeFile =
            UploadFile.UploadLargeFile
                .newBuilder()
                .setInfo(
                    UploadFile.UploadFileInfo
                        .newBuilder()
                        .setFileName(file.name)
                        .setExtension(file.extension)
                        .setTotalSize(file.length())
                        .setRoomId(ObjectId.get().toHexString())
                        .setUserId(ObjectId.get().toHexString()),
                ).build()

        private fun createChunkUploadVO(chunk: ByteArray): UploadFile.UploadLargeFile =
            UploadFile.UploadLargeFile
                .newBuilder()
                .setChunk(ByteString.copyFrom(chunk))
                .build()
    }
