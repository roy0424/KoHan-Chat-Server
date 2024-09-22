package com.kohan.message.rest.exception.code

import com.kohan.shared.armeria.exception.BusinessException
import com.linecorp.armeria.common.HttpStatus

enum class UserErrorCode(
    val businessException: BusinessException,
    ) {
    NOT_FOUND_USER(
        BusinessException(
            HttpStatus.BAD_REQUEST,
            mapOf("code" to "USER_002", "message" to "Not found user with condition"),
        ),
    ),
    UNAUTHORIZED(
        BusinessException(
            HttpStatus.UNAUTHORIZED,
            mapOf("code" to "USER_001", "message" to "Unauthorized"),
        ),
    ),
}