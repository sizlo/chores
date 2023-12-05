package com.timsummertonbrier.chores.controller.rest

import com.timsummertonbrier.chores.database.TaskRepository
import com.timsummertonbrier.chores.domain.Task
import com.timsummertonbrier.chores.domain.TaskRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post

@Controller("/tasks", consumes = [MediaType.APPLICATION_JSON])
open class TaskRestController(private val taskRepository: TaskRepository) {

    @Post
    open fun addTask(@Body taskRequest: TaskRequest): Task {
        val id = taskRepository.addTask(taskRequest)
        return taskRepository.findById(id)
    }
}