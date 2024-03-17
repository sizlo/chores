package com.timsummertonbrier.chores.service

import com.timsummertonbrier.chores.database.ExposedTransactional
import com.timsummertonbrier.chores.database.TaskCompletionRepository
import com.timsummertonbrier.chores.database.TaskRepository
import com.timsummertonbrier.chores.domain.Completion
import com.timsummertonbrier.chores.utils.atStartOfDay
import com.timsummertonbrier.chores.utils.now
import jakarta.inject.Singleton

@Singleton
@ExposedTransactional
class TaskCompleter(
    private val taskRepository: TaskRepository,
    private val taskCompletionRepository: TaskCompletionRepository
) {
    fun complete(taskId: Int, autocomplete: Boolean = false): Completion {
        if (taskWasCompletedToday(taskId)) {
            throw RuntimeException("Cannot complete task with id=$taskId because it was already completed today")
        }

        val input = taskCompletionRepository.findCompletionInputByTaskId(taskId)

        val completion = Completion(
            taskId = taskId,
            completionTimestamp = now(),
            dueDateWhenCompleted = input.currentDueDate,
            wasAutocomplete = autocomplete
        )

        taskRepository.updateDueDate(taskId, input.trigger.calculateNextDueDate())
        taskCompletionRepository.addCompletion(completion)

        return completion
    }

    fun taskWasCompletedToday(taskId: Int): Boolean {
        val latestCompletion = taskCompletionRepository.findLatestCompletionForTaskId(taskId) ?: return false
        return latestCompletion.completionTimestamp.atStartOfDay() == now().atStartOfDay()
    }
}