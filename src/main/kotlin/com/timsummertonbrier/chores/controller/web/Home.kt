package com.timsummertonbrier.chores.controller.web

import com.timsummertonbrier.chores.database.TaskRepository
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.views.View

@Controller
class HomeWebController(private val taskRepository: TaskRepository) {

    @Get("/")
    @View("home")
    fun home(): HttpResponse<Any> {
        return HttpResponse.ok(mapOf(
            "overdueTasks" to taskRepository.getOverdueTasksForHomePage()
        ))
    }
}