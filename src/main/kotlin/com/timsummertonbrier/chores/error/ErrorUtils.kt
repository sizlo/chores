package com.timsummertonbrier.chores.error

import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.cookie.Cookie

fun <T> MutableHttpResponse<T>.resetErrorCookies(): HttpResponse<T> {
    return this
        .cookie(Cookie.of("errors", ""))
        .cookie(Cookie.of("erroringBody", ""))
}