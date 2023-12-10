package com.timsummertonbrier.chores.service

import com.timsummertonbrier.chores.database.ExposedTransactional
import com.timsummertonbrier.chores.database.TaskCompletionRepository
import com.timsummertonbrier.chores.database.TaskRepository
import com.timsummertonbrier.chores.domain.Completion
import com.timsummertonbrier.chores.utils.now
import jakarta.inject.Singleton

@Singleton
@ExposedTransactional
class TaskCompleter(
    private val taskRepository: TaskRepository,
    private val taskCompletionRepository: TaskCompletionRepository
) {
    fun complete(taskId: Int, autocomplete: Boolean = false): Completion {
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
}