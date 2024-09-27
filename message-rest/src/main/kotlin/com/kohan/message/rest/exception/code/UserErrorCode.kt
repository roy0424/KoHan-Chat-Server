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
    NOT_FOUND_CHAT_ROOM(
        BusinessException(
            HttpStatus.NOT_FOUND,
            mapOf("code" to "USER_003", "message" to "Not found chatroom with condition"),
        ),
    ),
    NOT_IN_CHAT_ROOM(
        BusinessException(
            HttpStatus.BAD_REQUEST,
            mapOf("code" to "USER_004", "message" to "Not in chatroom"),
        ),
    ),
    REQUEST_USER_NOT_IN_CHAT_ROOM(
        BusinessException(
            HttpStatus.BAD_REQUEST,
            mapOf("code" to "USER_005", "message" to "Request user not in chatroom"),
        ),
    ),
    ALREADY_IN_CHAT_ROOM(
        BusinessException(
            HttpStatus.BAD_REQUEST,
            mapOf("code" to "USER_006", "message" to "Already in chatroom"),
        ),
    ),
    NOT_GROUP_CHAT_ROOM(
        BusinessException(
            HttpStatus.BAD_REQUEST,
            mapOf("code" to "USER_007", "message" to "Not group chatroom"),
        ),
    ),
    USER_NOT_LEAVING_USER(
        BusinessException(
            HttpStatus.BAD_REQUEST,
            mapOf("code" to "USER_008", "message" to "User not leaving user"),
        ),
    ),
}