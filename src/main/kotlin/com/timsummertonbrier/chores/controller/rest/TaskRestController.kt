package com.timsummertonbrier.chores.controller.rest

import com.timsummertonbrier.chores.database.TaskRepository
import com.timsummertonbrier.chores.domain.Task
import com.timsummertonbrier.chores.domain.TaskRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import jakarta.validation.Valid

@Controller("/api/tasks", consumes = [MediaType.APPLICATION_JSON])
open class TaskRestController(private val taskRepository: TaskRepository) {

    @Post
    open fun addTask(@Valid @Body taskRequest: TaskRequest): Task {
        val id = taskRepository.addTask(taskRequest)
        return taskRepository.findById(id)
    }

    @Post("/{id}")
    open fun updateTask(@PathVariable id: Int, @Valid @Body taskRequest: TaskRequest): Task {
        taskRepository.updateTask(id, taskRequest)
        return taskRepository.findById(id)
    }

    @Get("/{id}")
    fun getTask(@PathVariable id: Int): Task {
        return taskRepository.findById(id)
    }

    @Delete("/{id}")
    fun deleteTask(@PathVariable id: Int) {
        taskRepository.deleteTask(id)
    }
}