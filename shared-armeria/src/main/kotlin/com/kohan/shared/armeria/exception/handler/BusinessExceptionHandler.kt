package com.kohan.shared.armeria.exception.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.kohan.shared.armeria.exception.BusinessException
import com.linecorp.armeria.common.HttpData
import com.linecorp.armeria.common.HttpRequest
import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.common.MediaType
import com.linecorp.armeria.server.ServiceRequestContext
import com.linecorp.armeria.server.annotation.ExceptionHandlerFunction

class BusinessExceptionHandler : ExceptionHandlerFunction {
    override fun handleException(
        ctx: ServiceRequestContext,
        req: HttpRequest,
        cause: Throwable,
    ): HttpResponse {
        val exception = cause as? BusinessException ?: return ExceptionHandlerFunction.fallthrough()

        val mapper = ObjectMapper()
        return HttpResponse.of(exception.status, MediaType.JSON, HttpData.ofUtf8(mapper.writeValueAsString(exception.body)))
    }
}
