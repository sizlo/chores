package com.timsummertonbrier.chores.database

import com.timsummertonbrier.chores.domain.Completion
import com.timsummertonbrier.chores.domain.TaskRequest
import com.timsummertonbrier.chores.domain.TriggerType
import com.timsummertonbrier.chores.utils.minusDays
import com.timsummertonbrier.chores.utils.now
import com.timsummertonbrier.chores.utils.plusDays
import com.timsummertonbrier.chores.utils.today
import io.micronaut.context.annotation.Requires
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton

@Singleton
@Requires(notEnv = ["raspberrypi", "test"])
@ExposedTransactional
class DefaultDataInserter(
    private val taskRepository: TaskRepository,
    private val taskCompletionRepository: TaskCompletionRepository
) {
    @EventListener
    @Suppress("UNUSED_PARAMETER")
    fun insertDefaultData(event: DatabaseReadyEvent) {
        if (taskRepository.getAllTasksForAllTasksPage().isNotEmpty()) {
            return
        }

        taskRepository.addTask(
            TaskRequest(
                name = "due yesterday fixed",
                dueDate = today().minusDays(1).toString(),
                autocomplete = false,
                triggerType = TriggerType.FIXED_DELAY.name,
                daysBetween = "5",
            )
        )

        taskRepository.addTask(
            TaskRequest(
                name = "due yesterday weekly",
                dueDate = today().minusDays(1).toString(),
                autocomplete = false,
                triggerType = TriggerType.WEEKLY.name,
                dayOfWeek = "2",
            )
        )

        taskRepository.addTask(
            TaskRequest(
                name = "due yesterday monthly",
                dueDate = today().minusDays(1).toString(),
                autocomplete = false,
                triggerType = TriggerType.MONTHLY.name,
                dayOfMonth = "15",
            )
        )

        taskRepository.addTask(
            TaskRequest(
                name = "due yesterday yearly",
                dueDate = today().minusDays(1).toString(),
                autocomplete = false,
                triggerType = TriggerType.YEARLY.name,
                monthOfYear = "3",
                dayOfMonth = "15",
            )
        )

        taskRepository.addTask(
            TaskRequest(
                name = "due yesterday one off",
                dueDate = today().minusDays(1).toString(),
                autocomplete = false,
                triggerType = TriggerType.ONE_OFF.name,
            )
        )

        taskRepository.addTask(
            TaskRequest(
                name = "due today",
                dueDate = today().toString(),
                autocomplete = false,
                triggerType = TriggerType.FIXED_DELAY.name,
                daysBetween = "5",
            )
        )

        taskRepository.addTask(
            TaskRequest(
                name = "due tomorrow",
                dueDate = today().plusDays(1).toString(),
                autocomplete = false,
                triggerType = TriggerType.FIXED_DELAY.name,
                daysBetween = "5",
            )
        )

        taskRepository.addTask(
            TaskRequest(
                name = "no due date",
                dueDate = null,
                autocomplete = false,
                triggerType = TriggerType.FIXED_DELAY.name,
                daysBetween = "5",
            )
        )

        taskRepository.addTask(
            TaskRequest(
                name = "to be autocompleted",
                dueDate = today().minusDays(1).toString(),
                autocomplete = true,
                triggerType = TriggerType.FIXED_DELAY.name,
                daysBetween = "20",
            )
        )

        taskRepository.addTask(
            TaskRequest(
                name = "an overdue task with a very long name to see how our styling copes with lots of text which may need to wrap onto a new line",
                dueDate = today().minusDays(300).toString(),
                autocomplete = false,
                triggerType = TriggerType.FIXED_DELAY.name,
                daysBetween = "20",
            )
        )

        val completedTodayTaskId = taskRepository.addTask(
            TaskRequest(
                name = "completed today",
                dueDate = today().plusDays(10).toString(),
                autocomplete = false,
                triggerType = TriggerType.FIXED_DELAY.name,
                daysBetween = "5",
            )
        )

        val completedYesterdayTaskId = taskRepository.addTask(
            TaskRequest(
                name = "completed yesterday",
                dueDate = today().plusDays(10).toString(),
                autocomplete = false,
                triggerType = TriggerType.FIXED_DELAY.name,
                daysBetween = "5",
            )
        )

        // To be used by uncomplete
        taskCompletionRepository.addCompletion(
            Completion(
                taskId = completedTodayTaskId,
                completionTimestamp = now(),
                dueDateWhenCompleted = today().minusDays(1),
                wasAutocomplete = false
            )
        )

        // To be ignored by uncomplete because it is not the latest
        taskCompletionRepository.addCompletion(
            Completion(
                taskId = completedTodayTaskId,
                completionTimestamp = now().minusDays(10),
                dueDateWhenCompleted = today().minusDays(10),
                wasAutocomplete = false
            )
        )

        // To be ignored by uncomplete because it is an autocomplete
        taskCompletionRepository.addCompletion(
            Completion(
                taskId = completedTodayTaskId,
                completionTimestamp = now(),
                dueDateWhenCompleted = today().minusDays(10),
                wasAutocomplete = true
            )
        )

        taskCompletionRepository.addCompletion(
            Completion(
                taskId = completedYesterdayTaskId,
                completionTimestamp = now().minusDays(1),
                dueDateWhenCompleted = today().minusDays(10),
                wasAutocomplete = false
            )
        )
    }
}
