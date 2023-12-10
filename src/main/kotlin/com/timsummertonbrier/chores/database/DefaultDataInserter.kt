package com.timsummertonbrier.chores.database

import com.timsummertonbrier.chores.domain.Completion
import com.timsummertonbrier.chores.domain.TaskRequest
import com.timsummertonbrier.chores.domain.TriggerType
import com.timsummertonbrier.chores.utils.now
import com.timsummertonbrier.chores.utils.today
import io.micronaut.context.annotation.Requires
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton
import kotlinx.datetime.*
import kotlin.time.Duration.Companion.days

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
                dueDate = today().minus(DatePeriod(days = 1)).toString(),
                autocomplete = false,
                triggerType = TriggerType.FIXED_DELAY.name,
                daysBetween = "5",
            )
        )

        taskRepository.addTask(
            TaskRequest(
                name = "due yesterday weekly",
                dueDate = today().minus(DatePeriod(days = 1)).toString(),
                autocomplete = false,
                triggerType = TriggerType.WEEKLY.name,
                dayOfWeek = "2",
            )
        )

        taskRepository.addTask(
            TaskRequest(
                name = "due yesterday monthly",
                dueDate = today().minus(DatePeriod(days = 1)).toString(),
                autocomplete = false,
                triggerType = TriggerType.MONTHLY.name,
                dayOfMonth = "15",
            )
        )

        taskRepository.addTask(
            TaskRequest(
                name = "due yesterday yearly",
                dueDate = today().minus(DatePeriod(days = 1)).toString(),
                autocomplete = false,
                triggerType = TriggerType.YEARLY.name,
                monthOfYear = "3",
                dayOfMonth = "15",
            )
        )

        taskRepository.addTask(
            TaskRequest(
                name = "due yesterday one off",
                dueDate = today().minus(DatePeriod(days = 1)).toString(),
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
                dueDate = today().plus(DatePeriod(days = 1)).toString(),
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
                dueDate = today().minus(DatePeriod(days = 1)).toString(),
                autocomplete = true,
                triggerType = TriggerType.FIXED_DELAY.name,
                daysBetween = "20",
            )
        )

        taskRepository.addTask(
            TaskRequest(
                name = "an overdue task with a very long name to see how our styling copes with lots of text which may need to wrap onto a new line",
                dueDate = today().minus(DatePeriod(days = 300)).toString(),
                autocomplete = false,
                triggerType = TriggerType.FIXED_DELAY.name,
                daysBetween = "20",
            )
        )

        val completedTodayTaskId = taskRepository.addTask(
            TaskRequest(
                name = "completed today",
                dueDate = today().plus(DatePeriod(days = 10)).toString(),
                autocomplete = false,
                triggerType = TriggerType.FIXED_DELAY.name,
                daysBetween = "5",
            )
        )

        val completedYesterdayTaskId = taskRepository.addTask(
            TaskRequest(
                name = "completed yesterday",
                dueDate = today().plus(DatePeriod(days = 10)).toString(),
                autocomplete = false,
                triggerType = TriggerType.FIXED_DELAY.name,
                daysBetween = "5",
            )
        )

        taskCompletionRepository.addCompletion(
            Completion(
                taskId = completedTodayTaskId,
                completionTimestamp = now(),
                dueDateWhenCompleted = today().minus(DatePeriod(days = 1)),
                wasAutocomplete = false
            )
        )

        taskCompletionRepository.addCompletion(
            Completion(
                taskId = completedTodayTaskId,
                completionTimestamp = now().minus(10.days),
                dueDateWhenCompleted = today().minus(DatePeriod(days = 10)),
                wasAutocomplete = false
            )
        )

        taskCompletionRepository.addCompletion(
            Completion(
                taskId = completedTodayTaskId,
                completionTimestamp = now(),
                dueDateWhenCompleted = today().minus(DatePeriod(days = 10)),
                wasAutocomplete = true
            )
        )

        taskCompletionRepository.addCompletion(
            Completion(
                taskId = completedYesterdayTaskId,
                completionTimestamp = now().minus(1.days),
                dueDateWhenCompleted = today().minus(DatePeriod(days = 10)),
                wasAutocomplete = false
            )
        )
    }
}
