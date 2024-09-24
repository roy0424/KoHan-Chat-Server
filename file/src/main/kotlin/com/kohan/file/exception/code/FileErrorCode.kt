package com.kohan.file.exception.code

import com.kohan.shared.armeria.exception.BusinessException
import com.linecorp.armeria.common.HttpStatus

enum class FileErrorCode(
    val businessException: BusinessException,
) {
    EXCEEDED_TOTAL_SIZE(
        BusinessException(
            HttpStatus.BAD_REQUEST,
            mapOf("code" to "FILE_001", "message" to "More data was uploaded than the total size"),
        ),
    ),

    NOT_RECEIVING_FILE_INFO(
        BusinessException(
            HttpStatus.BAD_REQUEST,
            mapOf("code" to "FILE_002", "message" to "Before upload a file, you must send file info."),
        ),
    ),

    EXCEEDED_MAXIMUM_UPLOAD_SIZE(
        BusinessException(
            HttpStatus.BAD_REQUEST,
            mapOf("code" to "FILE_003", "message" to "You have exceeded the maximum upload size."),
        ),
    ),
}
