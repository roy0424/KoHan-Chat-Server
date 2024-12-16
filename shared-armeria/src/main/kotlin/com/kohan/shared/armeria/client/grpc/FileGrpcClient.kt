package com.kohan.shared.armeria.client.grpc

import com.google.protobuf.ByteString
import com.kohan.proto.file.v1.FileUploadServiceGrpcKt
import com.kohan.proto.file.v1.UploadFile
import com.linecorp.armeria.client.grpc.GrpcClients
import io.github.cdimascio.dotenv.dotenv
import java.io.File

object FileGrpcClient {
    private val port = dotenv()["FILE_PORT"]

    private val client =
        GrpcClients.newClient(
            "gproto+http://localhost:$port/grpc/v1/",
            FileUploadServiceGrpcKt.FileUploadServiceCoroutineStub::class.java,
        )

    suspend fun uploadProfile(
        file: File,
        userId: String,
    ): String =
        client
            .uploadProfile(
                UploadFile.UploadProfile
                    .newBuilder()
                    .setFileContent(ByteString.copyFrom(file.readBytes()))
                    .setUserId(userId)
                    .setTotalSize(file.length().toInt())
                    .build(),
            ).fileId
}
