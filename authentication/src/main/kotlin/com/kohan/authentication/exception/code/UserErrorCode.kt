package com.kohan.authentication.exception.code

import com.kohan.shared.armeria.exception.BusinessException
import com.linecorp.armeria.common.HttpStatus

enum class UserErrorCode(val businessException: BusinessException) {
    DUPLICATED_EMAIL(
        BusinessException(
            HttpStatus.BAD_REQUEST,
            mapOf("code" to "USER_001", "message" to "Duplicated email"),
        ),
    ),
    NOT_FOUND_USER(
        BusinessException(
            HttpStatus.BAD_REQUEST,
            mapOf("code" to "USER_002", "message" to "Not found user with condition"),
        ),
    ),
    INVALID_TOKEN(
        BusinessException(
            HttpStatus.BAD_REQUEST,
            mapOf("code" to "USER_003", "message" to "Invalid token"),
        ),
    ),
}
