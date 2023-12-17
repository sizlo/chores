package com.timsummertonbrier.chores.controller.rest

import com.timsummertonbrier.chores.database.TaskRepository
import com.timsummertonbrier.chores.domain.*
import com.timsummertonbrier.chores.service.Autocompleter
import com.timsummertonbrier.chores.service.CompletionReverter
import com.timsummertonbrier.chores.service.TaskCompleter
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.Valid

@Controller("/api/tasks", consumes = [MediaType.APPLICATION_JSON])
open class TaskRestController(
    private val taskRepository: TaskRepository,
    private val taskCompleter: TaskCompleter,
    private val completionReverter: CompletionReverter,
    private val autocompleter: Autocompleter,
) {

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

    @Delete("/{id}")
    fun deleteTask(@PathVariable id: Int) {
        taskRepository.deleteTask(id)
    }

    @Post("/{id}/complete")
    fun completeTask(@PathVariable id: Int): CompletionResponse {
        val completion = taskCompleter.complete(id)
        return CompletionResponse(
            task = taskRepository.findById(id),
            completion = completion
        )
    }

    @Post("/{id}/uncomplete")
    fun uncompleteTask(@PathVariable id: Int): Task {
        completionReverter.revertLatestCompletion(id)
        return taskRepository.findById(id)
    }

    @Post("/autocomplete")
    fun autocomplete(): List<Completion> {
        return autocompleter.autocomplete()
    }

    @Get("/{id}")
    fun getTask(@PathVariable id: Int): Task {
        return taskRepository.findById(id)
    }

    @Get("/")
    fun getAllTasks(): List<AllTasksTaskView> {
        return taskRepository.getAllTasksForAllTasksPage()
    }

    @Get("/overdue")
    fun getOverdueTasks(): List<OverdueTasksTaskView> {
        return taskRepository.getOverdueTasksForHomePage()
    }

    @Get("/completed-today")
    fun getCompletedTodayTasks(): List<CompletedTodayTasksTaskView> {
        return taskRepository.getCompletedTodayTasksForHomePage()
    }

    @Get("/tasks-for-autocompletion")
    fun getTasksForAutocompletion(): List<Int> {
        return taskRepository.getTasksForAutocompletion()
    }

    @Serdeable
    data class CompletionResponse(val task: Task, val completion: Completion)
}