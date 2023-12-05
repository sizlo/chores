package com.timsummertonbrier.chores

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.views.View

@Controller
class HomeController {

    @Get("/")
    @View("home")
    fun home(): HttpResponse<Any> {
        return HttpResponse.ok(mapOf(
            "message" to "Hello World!"
        ))
    }
}