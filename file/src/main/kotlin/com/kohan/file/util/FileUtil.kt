package com.kohan.file.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.coobird.thumbnailator.Thumbnails
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.awt.image.BufferedImage
import java.io.File
import java.io.OutputStream

@Component
class FileUtil(
    @Value("\${kohan.file.savePath}")
    private val savePath: String,
    @Value("\${kohan.file.profileExtensions}")
    private val profileExtension: String,
) {
    suspend fun writeToFile(
        byteArray: ByteArray,
        outputStream: OutputStream,
    ) {
        withContext(Dispatchers.IO) {
            outputStream.write(byteArray)
        }
    }

    suspend fun saveCompressedImage(
        fileName: String,
        image: BufferedImage,
    ) {
        val newProfile = File(savePath, fileName)
        withContext(Dispatchers.IO) {
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

    fun newFile(fileName: String): File = File(savePath, fileName)
}
