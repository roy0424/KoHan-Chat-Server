package com.kohan.shared.spring.exception.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.linecorp.armeria.common.HttpData
import com.linecorp.armeria.common.HttpRequest
import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.common.HttpStatus
import com.linecorp.armeria.common.MediaType
import com.linecorp.armeria.server.ServiceRequestContext
import com.linecorp.armeria.server.annotation.ExceptionHandlerFunction

class MismatchedInputExceptionHandler : ExceptionHandlerFunction {
    override fun handleException(
        ctx: ServiceRequestContext,
        req: HttpRequest,
        cause: Throwable,
    ): HttpResponse {
        val exception = cause.cause as? MismatchedInputException ?: return ExceptionHandlerFunction.fallthrough()

        val mapper = ObjectMapper()
        val errors =
            exception.path.map {
                    path ->
                mapOf(path.fieldName to "${path.fieldName} field is missing")
            }.reversed()

        return HttpResponse.of(HttpStatus.BAD_REQUEST, MediaType.JSON, HttpData.ofUtf8(mapper.writeValueAsString(errors)))
    }
}
