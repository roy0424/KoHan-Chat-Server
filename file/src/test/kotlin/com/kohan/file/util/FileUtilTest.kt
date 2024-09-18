package com.kohan.file.util

import com.google.protobuf.ByteString
import com.kohan.file.repository.FileRepository
import com.kohan.shared.armeria.file.v1.FileUploadServiceGrpcKt
import com.kohan.shared.armeria.file.v1.UploadFile.UploadFileDTO
import com.kohan.shared.armeria.file.v1.UploadFile.UploadFileInfo
import com.kohan.shared.armeria.file.v1.UploadFile.UploadLageFileVO
import com.linecorp.armeria.client.grpc.GrpcClients
import com.linecorp.armeria.common.grpc.GrpcSerializationFormats
import io.grpc.StatusException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.test.annotation.DirtiesContext
import java.io.BufferedInputStream
import java.time.Duration
import java.util.UUID
import javax.imageio.ImageIO

@SpringBootTest
@DirtiesContext
@AutoConfigureDataMongo
@EnableAutoConfiguration
class FileUtilTest
    @Autowired
    constructor(
        private val fileUtil: FileUtil,
        private val fileRepository: FileRepository,
    ) {
        @Test
        fun uploadFileTest() =
            runTest {
                val testFile = ClassPathResource("/DummyFiles/test.txt")
                val newFile = fileUtil.newFile(UUID.randomUUID().toString())

                testFile.inputStream.buffered().use { testStream ->
                    newFile.outputStream().buffered().use { newStream ->
                        fileUtil.writeToFile(testStream.readBytes(), newStream)
                    }
                }

                testFile.inputStream.buffered().use { testStream ->
                    assertEquals(
                        testFile.inputStream.buffered().use { testStream.readBytes().toString(Charsets.UTF_8) },
                        newFile.readBytes().toString(Charsets.UTF_8),
                    )
                }
            }

        @Test
        fun saveCompressedImage() =
            runTest {
                val testFile = ClassPathResource("/DummyFiles/test.jpg")
                val image = testFile.inputStream.buffered().use(ImageIO::read)

                val newFileName = UUID.randomUUID().toString()
                fileUtil.saveCompressedImage(newFileName, image)

                testFile.inputStream.buffered().use { testStream ->
                    val savedFile = fileUtil.newFile(newFileName)
                    assertTrue(
                        testStream.readBytes().size > savedFile.length(),
                    )
                }
            }

        @Test
        fun uploadLageFileTest() =
            runTest {
                val testFile = ClassPathResource("/DummyFiles/22mb.txt")

                val initialUploadVO =
                    testFile.inputStream.buffered().use { testStream ->
                        createInitialUploadVO(
                            fileName = "22mb",
                            fileExtension = "txt",
                            fileSize = testStream.readBytes().size.toLong(),
                        )
                    }

                val savedFileKey =
                    testFile.inputStream.buffered().use { testStream ->
                        uploadFile(testStream, false, initialUploadVO)
                    }

                assertTrue(savedFileKey != "")

                val fileCollection = fileRepository.findById(ObjectId(savedFileKey))
                assertTrue(fileCollection.isPresent)

                testFile.inputStream.buffered().use { testStream ->
                    val savedFile = fileUtil.newFile(fileCollection.get().fileName)
                    assertTrue(savedFile.exists())
                    assertTrue(savedFile.length() >= testStream.readBytes().size)
                }
            }

        @Test
        fun uploadLageImageTest() =
            runTest {
                val testFile = ClassPathResource("/DummyFiles/22mb.jpg")
                val initialUploadVO =
                    testFile.inputStream.buffered().use { testStream ->
                        createInitialUploadVO(
                            fileName = "22mb",
                            fileExtension = "jpg",
                            fileSize = testStream.readBytes().size.toLong(),
                        )
                    }

                val savedFileKey =
                    testFile.inputStream.buffered().use { testStream ->
                        uploadFile(testStream, true, initialUploadVO)
                    }

                assertTrue(savedFileKey != "")

                val fileCollection = fileRepository.findById(ObjectId(savedFileKey))
                assertTrue(fileCollection.isPresent)

                testFile.inputStream.buffered().use { testStream ->
                    val savedFile = fileUtil.newFile(fileCollection.get().fileName)
                    assertTrue(savedFile.exists())
                    assertTrue(savedFile.length() < testStream.readBytes().size)
                }
            }

        @Test
        fun totalSizeExceededText() =
            runTest {
                val testFile = ClassPathResource("/DummyFiles/22mb.jpg")
                val initialUploadVO =
                    testFile.inputStream.buffered().use { testStream ->
                        UploadLageFileVO
                            .newBuilder()
                            .setInfo(
                                UploadFileInfo
                                    .newBuilder()
                                    .setFileName("22mb")
                                    .setExtension("jpg")
                                    .setTotalSize(testStream.readBytes().size.toLong() - (1024 * 1024 * 32))
                                    .setRoomKey(ObjectId.get().toHexString())
                                    .setUserKey(ObjectId.get().toHexString()),
                            ).build()
                    }

                testFile.inputStream.buffered().use { testStream ->
                    assertThrows<StatusException> {
                        uploadFile(testStream, false, initialUploadVO)
                    }
                }
            }

        @Test
        fun notSendingFileInfoTest() =
            runTest {
                val testFile = ClassPathResource("/DummyFiles/22mb.jpg")

                testFile.inputStream.buffered().use { testStream ->
                    assertThrows<StatusException> {
                        val client = createGrpcClient()
                        val request = createFileUploadFlow(testStream, null)
                        val responses = client.uploadLageFile(request)
                        handleUploadResponses(responses)
                    }
                }
            }

        private suspend fun uploadFile(
            fileStream: BufferedInputStream,
            imageCompressing: Boolean,
            initialUploadVO: UploadLageFileVO,
        ): String {
            val client = createGrpcClient()
            val request = createFileUploadFlow(fileStream, initialUploadVO)
            val responses =
                if (imageCompressing) {
                    client.uploadLageImage(request)
                } else {
                    client.uploadLageFile(request)
                }
            return handleUploadResponses(responses)
        }

        private fun createFileUploadFlow(
            fileStream: BufferedInputStream,
            initialUploadVO: UploadLageFileVO?,
        ): Flow<UploadLageFileVO> =
            flow {
                val chunkSize: Int = 1024 * 16

                if (initialUploadVO != null) {
                    emit(initialUploadVO)
                }

                fileStream.use { inputStream ->
                    val buffer = ByteArray(chunkSize)
                    var bytesRead: Int

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        val chunk = if (bytesRead == chunkSize) buffer else buffer.copyOf(bytesRead)
                        emit(createChunkUploadVO(chunk))
                    }
                }
            }

        private suspend fun handleUploadResponses(responses: Flow<UploadFileDTO>): String {
            var savedFileKey = ""
            responses.collect { response ->
                when {
                    response.hasReceived() and response.hasTotal() -> {
                        println("${response.received} / ${response.total}")
                    }

                    response.hasFileKey() -> {
                        savedFileKey = response.fileKey
                    }

                    response.hasMessage() -> {
                        throw IllegalStateException(response.message)
                    }
                }
            }
            return savedFileKey
        }

        private fun createGrpcClient(): FileUploadServiceGrpcKt.FileUploadServiceCoroutineStub =
            GrpcClients
                .builder("gproto+http://127.0.0.1:8080/grpc/v1/")
                .serializationFormat(GrpcSerializationFormats.PROTO)
                .responseTimeout(Duration.ZERO)
                .writeTimeout(Duration.ZERO)
                .maxRequestMessageLength(-1)
                .maxResponseMessageLength(-1)
                .build(FileUploadServiceGrpcKt.FileUploadServiceCoroutineStub::class.java)

        private fun createInitialUploadVO(
            fileName: String,
            fileExtension: String,
            fileSize: Long,
        ): UploadLageFileVO =
            UploadLageFileVO
                .newBuilder()
                .setInfo(
                    UploadFileInfo
                        .newBuilder()
                        .setFileName(fileName)
                        .setExtension(fileExtension)
                        .setTotalSize(fileSize)
                        .setRoomKey(ObjectId.get().toHexString())
                        .setUserKey(ObjectId.get().toHexString()),
                ).build()

        private fun createChunkUploadVO(chunk: ByteArray): UploadLageFileVO =
            UploadLageFileVO
                .newBuilder()
                .setChunk(ByteString.copyFrom(chunk))
                .build()
    }
