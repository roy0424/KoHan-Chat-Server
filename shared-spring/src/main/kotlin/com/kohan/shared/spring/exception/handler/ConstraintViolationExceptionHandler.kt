package com.kohan.shared.spring.exception.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.linecorp.armeria.common.HttpData
import com.linecorp.armeria.common.HttpRequest
import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.common.HttpStatus
import com.linecorp.armeria.common.MediaType
import com.linecorp.armeria.server.ServiceRequestContext
import com.linecorp.armeria.server.annotation.ExceptionHandlerFunction
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException

class ConstraintViolationExceptionHandler : ExceptionHandlerFunction {
    override fun handleException(
        ctx: ServiceRequestContext,
        req: HttpRequest,
        cause: Throwable,
    ): HttpResponse {
        val exception = cause as? ConstraintViolationException ?: return ExceptionHandlerFunction.fallthrough()

        val mapper = ObjectMapper()
        val errors =
            exception.constraintViolations.map {
                    violation ->
                violation.propertyPath.reduce { fir, _ -> fir }.toString()
                ConstraintViolation<*>::getMessage
            }

        return HttpResponse.of(HttpStatus.BAD_REQUEST, MediaType.JSON, HttpData.ofUtf8(mapper.writeValueAsString(errors)))
    }
}
