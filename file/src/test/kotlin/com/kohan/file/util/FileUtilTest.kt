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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URI
import java.net.URL
import java.nio.channels.Channels
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
        companion object {
            lateinit var testTxt: File
            lateinit var test22mbTxt: File

            lateinit var testJPG: File
            lateinit var test22mbJPG: File

            @BeforeAll
            @JvmStatic
            internal fun setUp(
                @Autowired fileUtil: FileUtil,
            ) {
                testTxt = fileUtil.newFile("testTxt")
                test22mbTxt = fileUtil.newFile("test22mbTxt")
                testJPG = fileUtil.newFile("testJPG")
                test22mbJPG = fileUtil.newFile("test22mbJPG")

                downloadFile(
                    URI
                        .create(
                            "https://raw.githubusercontent.com/patcher454/KoHan-Chat-Server/file-server/file/src/test/resources/DummyFiles/test.txt",
                        ).toURL(),
                    testTxt,
                )
                downloadFile(
                    URI
                        .create(
                            "https://raw.githubusercontent.com/patcher454/KoHan-Chat-Server/file-server/file/src/test/resources/DummyFiles/22mb.txt",
                        ).toURL(),
                    test22mbTxt,
                )
                downloadFile(
                    URI
                        .create(
                            "https://raw.githubusercontent.com/patcher454/KoHan-Chat-Server/file-server/file/src/test/resources/DummyFiles/test.jpg",
                        ).toURL(),
                    testJPG,
                )
                downloadFile(
                    URI
                        .create(
                            "https://raw.githubusercontent.com/patcher454/KoHan-Chat-Server/file-server/file/src/test/resources/DummyFiles/22mb.jpg",
                        ).toURL(),
                    test22mbJPG,
                )
            }

            @AfterAll
            @JvmStatic
            internal fun cleanUp() {
                testTxt.delete()
                test22mbTxt.delete()

                testJPG.delete()
                test22mbJPG.delete()
            }

            private fun downloadFile(
                url: URL,
                file: File,
            ) {
                url.openStream().use {
                    Channels.newChannel(it).use { rbc ->
                        FileOutputStream(file).use { fos ->
                            fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
                        }
                    }
                }
            }
        }

        @Test
        fun uploadFileTest() =
            runTest {
                val testFile = testTxt
                val testFileStream = testFile.inputStream()
                val newFile = fileUtil.newFile(UUID.randomUUID().toString())

                newFile.outputStream().buffered().use { stream ->
                    fileUtil.writeToFile(testFileStream.readBytes(), stream)
                }

                assertEquals(
                    testFile.readBytes().toString(Charsets.UTF_8),
                    newFile.readBytes().toString(Charsets.UTF_8),
                )
            }

        @Test
        fun saveCompressedImage() =
            runTest {
                val testFile = testJPG
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
                val testFile = test22mbTxt
                val initialUploadVO = createInitialUploadVO(testFile)
                val savedFileKey = uploadFile(testFile, false, initialUploadVO)

                assertTrue(savedFileKey != "")

                val fileCollection = fileRepository.findById(ObjectId(savedFileKey))
                assertTrue(fileCollection.isPresent)

                val savedFile = fileUtil.newFile(fileCollection.get().fileName)
                assertTrue(savedFile.exists())
                assertTrue(savedFile.length() >= testFile.length())
            }

        @Test
        fun uploadLageImageTest() =
            runTest {
                val testFile = test22mbJPG
                val initialUploadVO = createInitialUploadVO(testFile)
                val savedFileKey = uploadFile(testFile, true, initialUploadVO)

                assertTrue(savedFileKey != "")

                val fileCollection = fileRepository.findById(ObjectId(savedFileKey))
                assertTrue(fileCollection.isPresent)

                val savedFile = fileUtil.newFile(fileCollection.get().fileName)
                assertTrue(savedFile.exists())
                assertTrue(savedFile.length() < testFile.length())
            }

        @Test
        fun notSendingFileInfoTest() =
            runTest {
                val testFile = test22mbJPG

                assertThrows<StatusException> {
                    val client = createGrpcClient()
                    val request = createFileUploadFlow(testFile, null)
                    val responses = client.uploadLageFile(request)
                    handleUploadResponses(responses)
                }
            }

        @Test
        fun totalSizeExceededText() =
            runTest {
                val testFile = test22mbJPG
                val initialUploadVO =
                    UploadLageFileVO
                        .newBuilder()
                        .setInfo(
                            UploadFileInfo
                                .newBuilder()
                                .setFileName(testFile.name)
                                .setExtension(testFile.extension)
                                .setTotalSize(testFile.length() - (1024 * 1024 * 32))
                                .setRoomKey(ObjectId.get().toHexString())
                                .setUserKey(ObjectId.get().toHexString()),
                        ).build()

                assertThrows<StatusException> {
                    uploadFile(testFile, false, initialUploadVO)
                }
            }

        private suspend fun uploadFile(
            file: File,
            imageCompressing: Boolean,
            initialUploadVO: UploadLageFileVO,
        ): String {
            val client = createGrpcClient()
            val request = createFileUploadFlow(file, initialUploadVO)
            val responses =
                if (imageCompressing) {
                    client.uploadLageImage(request)
                } else {
                    client.uploadLageFile(request)
                }
            return handleUploadResponses(responses)
        }

        private fun createFileUploadFlow(
            file: File,
            initialUploadVO: UploadLageFileVO?,
        ): Flow<UploadLageFileVO> =
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

        private fun createInitialUploadVO(file: File): UploadLageFileVO =
            UploadLageFileVO
                .newBuilder()
                .setInfo(
                    UploadFileInfo
                        .newBuilder()
                        .setFileName(file.name)
                        .setExtension(file.extension)
                        .setTotalSize(file.length())
                        .setRoomKey(ObjectId.get().toHexString())
                        .setUserKey(ObjectId.get().toHexString()),
                ).build()

        private fun createChunkUploadVO(chunk: ByteArray): UploadLageFileVO =
            UploadLageFileVO
                .newBuilder()
                .setChunk(ByteString.copyFrom(chunk))
                .build()
    }
