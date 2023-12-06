package com.timsummertonbrier.chores.error

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.cookie.Cookie
import jakarta.validation.ConstraintViolationException
import java.util.*

@Controller
class GlobalErrorHandler {

    @Error(global = true)
    fun handleConstraintViolations(request: HttpRequest<Any>, exception: ConstraintViolationException): HttpResponse<Any> {
        val violations = exception.constraintViolations
            .filterNot { it.message.isNullOrBlank() }

        val classErrors = violations
            .filter { it.propertyPath == null }
            .map { it.message.capitalize() }

        val fieldErrors = violations
            .filter { it.propertyPath != null }
            .groupBy(
                keySelector = { it.propertyPath.last().name },
                valueTransform = { it.message.capitalize() }
            )

        return errorResponse(request, Errors(classErrors, fieldErrors))
    }

    fun errorResponse(request: HttpRequest<Any>, errors: Errors): HttpResponse<Any> {
        return if (request.path.startsWith("/api")) {
            HttpResponse.badRequest(errors)
        } else {
            redirectError(request, errors)
        }
    }

    fun redirectError(request: HttpRequest<Any>, errors: Errors): HttpResponse<Any> {
        val objectMapper = jacksonObjectMapper()

        return HttpResponse.seeOther<Any>(request.uri)
            .cookie(Cookie.of("errors", objectMapper.writeValueAsString(errors)))
            .cookie(Cookie.of("erroringBody", objectMapper.writeValueAsString(request.body.get())))
    }

    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

}