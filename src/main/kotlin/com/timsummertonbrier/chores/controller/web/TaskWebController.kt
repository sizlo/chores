package com.timsummertonbrier.chores.controller.web

import com.timsummertonbrier.chores.database.TaskRepository
import com.timsummertonbrier.chores.domain.TaskRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.views.View
import java.net.URI

@Controller("/tasks")
open class TaskWebController(private val taskRepository: TaskRepository) {

    @Get
    @View("all-tasks")
    fun viewAllTasks(): HttpResponse<Any> {
        return HttpResponse.ok(mapOf(
            "tasks" to "TODO"
        ))
    }

    @Get("/{id}")
    @View("task-details")
    fun viewTaskDetails(@PathVariable id: Int): HttpResponse<Any> {
        return HttpResponse.ok(mapOf(
            "task" to taskRepository.findById(id)
        ))
    }

    @Get("/add")
    @View("add-task")
    fun viewAddTaskForm(): HttpResponse<Any> {
        return HttpResponse.ok(mapOf(
            "taskRequest" to TaskRequest()
        ))
    }

    @Get("/{id}/edit")
    @View("edit-task")
    fun viewEditTaskForm(@PathVariable id: Int): HttpResponse<Any> {
        return HttpResponse.ok(mapOf(
            "id" to id,
            "taskRequest" to TaskRequest.fromTask(taskRepository.findById(id))
        ))
    }

    @Post("/add")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    open fun addTask(@Body taskRequest: TaskRequest): HttpResponse<Any> {
        val id = taskRepository.addTask(taskRequest)
        return HttpResponse.seeOther(URI.create("/tasks/$id"))
    }

    @Post("/{id}/edit")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    open fun editTask(@PathVariable id: Int, @Body taskRequest: TaskRequest): HttpResponse<Any> {
        taskRepository.updateTask(id, taskRequest)
        return HttpResponse.seeOther(URI.create("/tasks/$id"))
    }
}