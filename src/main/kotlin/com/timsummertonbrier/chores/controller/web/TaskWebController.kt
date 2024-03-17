package com.timsummertonbrier.chores.controller.web

import com.timsummertonbrier.chores.database.TaskRepository
import com.timsummertonbrier.chores.domain.TaskRequest
import com.timsummertonbrier.chores.error.resetErrorCookies
import com.timsummertonbrier.chores.service.CompletionReverter
import com.timsummertonbrier.chores.service.TaskCompleter
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.views.View
import jakarta.validation.Valid
import java.net.URI

@Controller("/tasks")
open class TaskWebController(
    private val taskRepository: TaskRepository,
    private val taskCompleter: TaskCompleter,
    private val completionReverter: CompletionReverter
) {

    @Get
    @View("all-tasks")
    fun viewAllTasks(): HttpResponse<Any> {
        return HttpResponse.ok(mapOf(
            "tasks" to taskRepository.getAllTasksForAllTasksPage()
        ))
    }

    @Get("/{id}")
    @View("task-details")
    fun viewTaskDetails(@PathVariable id: Int): HttpResponse<Any> {
        return HttpResponse.ok(mapOf(
            "task" to taskRepository.findById(id),
            "wasCompletedToday" to taskCompleter.taskWasCompletedToday(id)
        ))
    }

    @Get("/add")
    @View("add-task")
    fun viewAddTaskForm(request: HttpRequest<Any>): HttpResponse<Any> {
        val model = mutableMapOf<String, Any>()
        model.addErrorModelAttributesFromCookies(request)
        model.computeIfAbsent("taskRequest") { TaskRequest() }
        return HttpResponse.ok<Any>(model).resetErrorCookies()
    }

    @Get("/{id}/edit")
    @View("edit-task")
    fun viewEditTaskForm(@PathVariable id: Int, request: HttpRequest<Any>): HttpResponse<Any> {
        val model = mutableMapOf<String, Any>()
        model["id"] = id
        model.addErrorModelAttributesFromCookies(request)
        model.computeIfAbsent("taskRequest") { TaskRequest.fromTask(taskRepository.findById(id)) }
        return HttpResponse.ok<Any>(model).resetErrorCookies()
    }

    @Post("/add")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    open fun addTask(@Valid @Body taskRequest: TaskRequest): HttpResponse<Any> {
        val id = taskRepository.addTask(taskRequest)
        return HttpResponse.seeOther(URI.create("/tasks/$id"))
    }

    @Post("/{id}/edit")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    open fun editTask(@PathVariable id: Int, @Valid @Body taskRequest: TaskRequest): HttpResponse<Any> {
        taskRepository.updateTask(id, taskRequest)
        return HttpResponse.seeOther(URI.create("/tasks/$id"))
    }

    @Post("/{id}/delete")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun deleteTask(@PathVariable id: Int): HttpResponse<Any> {
        taskRepository.deleteTask(id)
        return HttpResponse.seeOther(URI.create("/tasks"))
    }

    @Post("/{id}/complete")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun completeTask(@PathVariable id: Int, @QueryValue callbackUrl: String?): HttpResponse<Any> {
        taskCompleter.complete(id)
        return HttpResponse.seeOther(URI.create(callbackUrl ?: "/"))
    }

    @Post("/{id}/uncomplete")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    fun uncompleteTask(@PathVariable id: Int): HttpResponse<Any> {
        completionReverter.revertLatestCompletion(id)
        return HttpResponse.seeOther(URI.create("/"))
    }
}