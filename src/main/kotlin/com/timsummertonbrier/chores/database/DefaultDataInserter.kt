package com.timsummertonbrier.chores.database

import com.timsummertonbrier.chores.domain.TaskRequest
import com.timsummertonbrier.chores.domain.TriggerType
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
import kotlinx.datetime.*

interface DefaultDataInserter {
    fun insertDefaultData()
}

@Singleton
@Requires(notEnv = ["raspberrypi"])
class DevDefaultDataInserter(private val taskRepository: TaskRepository) : DefaultDataInserter {
    override fun insertDefaultData() {
        if (taskRepository.getAllTasksForAllTasksPage().isNotEmpty()) {
            return
        }

        taskRepository.addTask(
            TaskRequest(
                name = "overdue-fixed",
                dueDate = LocalDate.today().minus(DatePeriod(days = 1)).toString(),
                autocomplete = false,
                triggerType = TriggerType.FIXED_DELAY.name,
                daysBetween = "5",
            )
        )

        taskRepository.addTask(
            TaskRequest(
                name = "overdue-weekly",
                dueDate = LocalDate.today().minus(DatePeriod(days = 1)).toString(),
                autocomplete = false,
                triggerType = TriggerType.WEEKLY.name,
                dayOfWeek = "2",
            )
        )

        taskRepository.addTask(
            TaskRequest(
                name = "overdue-monthly",
                dueDate = LocalDate.today().minus(DatePeriod(days = 1)).toString(),
                autocomplete = false,
                triggerType = TriggerType.MONTHLY.name,
                dayOfMonth = "15",
            )
        )

        taskRepository.addTask(
            TaskRequest(
                name = "overdue-yearly",
                dueDate = LocalDate.today().minus(DatePeriod(days = 1)).toString(),
                autocomplete = false,
                triggerType = TriggerType.YEARLY.name,
                monthOfYear = "3",
                dayOfMonth = "15",
            )
        )

        taskRepository.addTask(
            TaskRequest(
                name = "overdue-one-off",
                dueDate = LocalDate.today().minus(DatePeriod(days = 1)).toString(),
                autocomplete = false,
                triggerType = TriggerType.ONE_OFF.name,
            )
        )

        taskRepository.addTask(
            TaskRequest(
                name = "due-tomorrow",
                dueDate = LocalDate.today().plus(DatePeriod(days = 1)).toString(),
                autocomplete = false,
                triggerType = TriggerType.FIXED_DELAY.name,
                daysBetween = "5",
            )
        )

        taskRepository.addTask(
            TaskRequest(
                name = "no-due-date",
                dueDate = null,
                autocomplete = false,
                triggerType = TriggerType.FIXED_DELAY.name,
                daysBetween = "5",
            )
        )
    }
}

@Singleton
@Requires(env = ["raspberrypi"])
class NoopDefaultDataInserter : DefaultDataInserter {
    override fun insertDefaultData() {}
}

private fun LocalDate.Companion.today(): LocalDate {
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}