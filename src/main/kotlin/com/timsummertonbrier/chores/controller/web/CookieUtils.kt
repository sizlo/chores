package com.timsummertonbrier.chores.controller.web

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.timsummertonbrier.chores.domain.TaskRequest
import com.timsummertonbrier.chores.error.Errors
import io.micronaut.http.HttpRequest

fun <T> HttpRequest<T>.getCookie(name: String): String? {
    cookies.get(name)?.value?.let {
        if (it.isNotBlank()) {
            return it
        }
    }
    return null
}

fun MutableMap<String, Any>.addErrorModelAttributesFromCookies(request: HttpRequest<Any>) {
    val objectMapper = jacksonObjectMapper()

    request.getCookie("errors")?.let {
        this["errors"] = objectMapper.readValue(it, Errors::class.java)
    }

    request.getCookie("erroringBody")?.let {
        // This assumes that our erroringBody is always an instance of TaskRequest
        this["taskRequest"] = objectMapper.readValue(it, TaskRequest::class.java)
    }
}
