package com.timsummertonbrier.chores.service

import com.timsummertonbrier.chores.database.TaskRepository
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class AutocompleteScheduler(private val taskRepository: TaskRepository, private val taskCompleter: TaskCompleter) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 3 * * *")
    fun autocomplete() {
        logger.info("Running autocomplete scheduled job")
        val tasks = taskRepository.getOverdueAutocompleteTaskIds()
        logger.info("Found ${tasks.size} tasks to autocomplete")
        tasks.forEach {
            logger.info("Autocompleting task with id=$it")
            taskCompleter.complete(taskId = it, autocomplete = true)
        }
    }
}