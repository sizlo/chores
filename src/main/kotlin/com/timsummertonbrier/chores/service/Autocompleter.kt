package com.timsummertonbrier.chores.service

import com.timsummertonbrier.chores.database.TaskRepository
import io.micronaut.context.annotation.Requires
import com.timsummertonbrier.chores.domain.Completion
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.TaskScheduler
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class Autocompleter(private val taskRepository: TaskRepository, private val taskCompleter: TaskCompleter) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun autocomplete(): List<Completion> {
        logger.info("Running autocomplete scheduled job")
        val tasks = taskRepository.getTasksForAutocompletion()
        logger.info("Found ${tasks.size} tasks to autocomplete")
        return tasks.map {
            logger.info("Autocompleting task with id=$it")
            taskCompleter.complete(taskId = it, autocomplete = true)
        }
    }
}

@Singleton
@Requires(notEnv = ["test"])
class AutocompleteScheduler(private val taskScheduler: TaskScheduler, private val autocompleter: Autocompleter) {
    // When running on the raspberry pi methods with the @Scheduled annotation
    // are not executed. I believe this to be the issue
    // https://stackoverflow.com/a/71166615
    // Manually scheduling them fixes the problem
    @EventListener
    @Suppress("UNUSED_PARAMETER")
    fun scheduleAutocompleteCron(event: StartupEvent) {
        taskScheduler.schedule("0 3 * * *", autocompleter::autocomplete)
    }
}