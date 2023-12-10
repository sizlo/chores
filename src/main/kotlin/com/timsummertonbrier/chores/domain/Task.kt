package com.timsummertonbrier.chores.domain

import com.timsummertonbrier.chores.utils.today
import io.micronaut.serde.annotation.Serdeable
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

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

@Serdeable
data class AllTasksTaskView(
    val id: Int,
    val name: String,
    val dueDate: LocalDate?
)

@Serdeable
data class OverdueTasksTaskView(
    val id: Int,
    val name: String,
    val dueDate: LocalDate
) {
    private val overdueDays = dueDate.daysUntil(today())
    val overdueText = "$overdueDays ${if (overdueDays == 1) "day" else "days"} overdue"
}

@Serdeable
data class CompletedTodayTasksTaskView(
    val id: Int,
    val name: String
)
