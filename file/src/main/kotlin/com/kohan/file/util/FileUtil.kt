package com.kohan.file.util

import com.kohan.file.collection.file.FileCollection
import com.kohan.file.repository.FileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.coobird.thumbnailator.Thumbnails
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import javax.imageio.ImageIO

@Component
class FileUtil(
    @Value("\${kohan.file.savePath}")
    private val savePath: String,
    @Value("\${kohan.file.profileExtensions}")
    private val profileExtension: String,
    private val fileRepository: FileRepository,
) {
    suspend fun uploadFile(
        fileCollection: FileCollection,
        inputStream: InputStream,
    ): FileCollection {
        CoroutineScope(Dispatchers.IO).launch {
            saveByteArrayToFile(
                fileCollection.fileName,
                inputStream,
            )
        }

        val saved =
            withContext(Dispatchers.IO) {
                fileRepository.save(fileCollection)
            }

        return saved
    }

    suspend fun uploadProfile(
        fileCollection: FileCollection,
        inputStream: InputStream,
    ): FileCollection {
        CoroutineScope(Dispatchers.IO).launch {
            saveCompressedImage(fileCollection.fileName, profileExtension, inputStream)
        }

        val saved =
            withContext(Dispatchers.IO) {
                fileRepository.save(fileCollection)
            }

        return saved
    }

    protected fun saveByteArrayToFile(
        fileName: String,
        inputStream: InputStream,
    ) {
        val newFile = File(savePath, fileName)
        inputStream.use {
            Files.copy(it, newFile.toPath())
        }
    }

    protected fun saveCompressedImage(
        fileName: String,
        profileExtension: String,
        inputStream: InputStream,
    ) {
        val newProfile = File(savePath, fileName)
        val image = inputStream.use(ImageIO::read)

        Thumbnails
            .of(image)
            .scale(1.0)
            .outputQuality(0.5)
            .outputFormat(profileExtension)
            .toFile(newProfile)

        val savedFile = File(savePath, "$fileName.$profileExtension")

        // remove extension
        savedFile.renameTo(newProfile)
    }
}
