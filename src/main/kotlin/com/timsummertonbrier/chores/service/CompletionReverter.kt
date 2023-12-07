package com.timsummertonbrier.chores.service

import com.timsummertonbrier.chores.database.ExposedTransactional
import com.timsummertonbrier.chores.database.TaskCompletionRepository
import com.timsummertonbrier.chores.database.TaskRepository
import jakarta.inject.Singleton

@Singleton
@ExposedTransactional
class CompletionReverter(
    private val completionRepository: TaskCompletionRepository,
    private val taskRepository: TaskRepository
) {

    fun revertLatestCompletion(taskId: Int) {
        val completionToRevert = completionRepository.findLatestNonAutoCompletionForTaskId(taskId)
        taskRepository.updateDueDate(taskId, completionToRevert.dueDateWhenCompleted)
        completionRepository.deleteCompletion(completionToRevert.id!!)
        completionRepository.addRevertedCompletion(completionToRevert)
    }
}