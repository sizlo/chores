package com.timsummertonbrier.chores.domain

import com.timsummertonbrier.chores.domain.serde.EmptyStringToNull
import com.timsummertonbrier.chores.domain.validation.ValidDate
import com.timsummertonbrier.chores.domain.validation.ValidTriggerType
import com.timsummertonbrier.chores.domain.validation.ValidTriggerParameters
import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
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

data class OverdueTasksTaskView(
    val id: Int,
    val name: String,
    val daysOverdue: Int
)

@Serdeable
@ValidTriggerParameters
data class TaskRequest(
    @NotBlank
    val name: String? = null,

    @EmptyStringToNull
    val description: String? = null,

    @ValidDate
    @EmptyStringToNull
    val dueDate: String? = null,

    @NotNull
    val autocomplete: Boolean? = null,

    @NotNull
    @ValidTriggerType
    val triggerType: String? = null,

    @EmptyStringToNull
    val daysBetween: String? = null,

    @EmptyStringToNull
    val dayOfWeek: String? = null,

    @EmptyStringToNull
    val dayOfMonth: String? = null,

    @EmptyStringToNull
    val monthOfYear: String? = null,
) {
    fun triggerTypeAsEnum() = TriggerType.valueOf(triggerType!!)
    fun dueDateAsDate() = dueDate?.let { LocalDate.parse(it) }

    companion object {
        fun fromTask(task: Task): TaskRequest {
            val request = TaskRequest(
                task.name,
                task.description,
                task.dueDate?.toString(),
                task.autocomplete,
                task.trigger.triggerType.name,
                null,
                null,
                null,
                null,
            )

            return when (task.trigger) {
                is FixedDelayTrigger -> request.copy(daysBetween = task.trigger.daysBetween.toString())
                is WeeklyTrigger -> request.copy(dayOfWeek = task.trigger.dayOfWeek.toString())
                is MonthlyTrigger -> request.copy(dayOfMonth = task.trigger.dayOfMonth.toString())
                is YearlyTrigger -> request.copy(monthOfYear = task.trigger.monthOfYear.toString(), dayOfMonth = task.trigger.dayOfMonth.toString())
                else -> request
            }
        }
    }
}
