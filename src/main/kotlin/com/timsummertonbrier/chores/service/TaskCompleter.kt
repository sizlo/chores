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

    // TODO
    // There is a bug where when you try to uncomplete a task it sometimes uncompletes the wrong task
    // Reproduce by
    // 1. Reset database, let default data inserter populate it
    // 2. Complete "due yesterday fixed"
    // 3. Uncomplete "completed today"
    // Expected behaviour: "completed today" is removed from "Completed today" list, and added to
    //    "Overdue tasks" list
    // Actual behaviour: "due yesterday fixed" is removed from "Completed today" list, and
    //    "completed today" is added to "Overdue tasks" list.
    //    - "due yesterday fixed" has disappeared from the home page
    //    - "completed today" is now duplicated on the home page, appearing in "Overdue tasks"
    //      and "Completed today"

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