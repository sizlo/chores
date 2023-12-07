package com.timsummertonbrier.chores.domain

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

data class OverdueTasksTaskView(
    val id: Int,
    val name: String,
    val daysOverdue: Int
)

data class CompletedTodayTasksTaskView(
    val id: Int,
    val name: String
)
