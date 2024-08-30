package com.kohan.shared.armeria.exception

import com.linecorp.armeria.common.HttpStatus
import java.io.Serial
import java.lang.RuntimeException

class BusinessException(
    val status: HttpStatus,
    val body: MutableMap<String, String>,
) : RuntimeException() {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 7505841777916502913L
    }
}
