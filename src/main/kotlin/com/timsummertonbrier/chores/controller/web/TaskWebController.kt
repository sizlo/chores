package com.timsummertonbrier.chores.controller.web

import com.timsummertonbrier.chores.database.TaskRepository
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.views.View

@Controller("/tasks")
class TaskWebController(private val taskRepository: TaskRepository) {

    @Get("/{id}")
    @View("task-details")
    fun viewTaskDetails(@PathVariable id: Int): HttpResponse<*> {
        return HttpResponse.ok(mapOf(
            "task" to taskRepository.findById(id)
        ))
    }
}