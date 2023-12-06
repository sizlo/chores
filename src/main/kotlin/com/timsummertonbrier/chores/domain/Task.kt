package com.timsummertonbrier.chores.domain

import com.timsummertonbrier.chores.domain.serde.EmptyStringToNullInt
import io.micronaut.serde.annotation.Serdeable
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Serdeable
data class Task(
    val id: Int,
    val name: String,
    val description: String?,
    val dueDate: LocalDate?,
    val autocomplete: Boolean,
    val createdTimestamp: Instant,
    val updatedTimestamp: Instant,
    val trigger: Trigger
)

data class AllTasksTaskView(
    val id: Int,
    val name: String,
    val dueDate: LocalDate?
)

@Serdeable
data class TaskRequest(
    val name: String? = null,

    val description: String? = null,

    val dueDate: LocalDate? = null,

    val autocomplete: Boolean = false,

    val triggerType: TriggerType? = null,

    @EmptyStringToNullInt
    val daysBetween: Int? = null,

    @EmptyStringToNullInt
    val dayOfWeek: Int? = null,

    @EmptyStringToNullInt
    val dayOfMonth: Int? = null,

    @EmptyStringToNullInt
    val monthOfYear: Int? = null,
) {
    fun triggerTypeAsString() = triggerType?.name

    companion object {
        fun fromTask(task: Task): TaskRequest {
            val request = TaskRequest(
                task.name,
                task.description,
                task.dueDate,
                task.autocomplete,
                task.trigger.triggerType,
                null,
                null,
                null,
                null,
            )

            return when (task.trigger) {
                is FixedDelayTrigger -> request.copy(daysBetween = task.trigger.daysBetween)
                is WeeklyTrigger -> request.copy(dayOfWeek = task.trigger.dayOfWeek)
                is MonthlyTrigger -> request.copy(dayOfMonth = task.trigger.dayOfMonth)
                is YearlyTrigger -> request.copy(monthOfYear = task.trigger.monthOfYear, dayOfMonth = task.trigger.dayOfMonth)
                else -> request
            }
        }
    }
}
