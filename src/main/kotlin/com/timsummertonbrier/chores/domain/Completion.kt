package com.timsummertonbrier.chores.domain

import io.micronaut.serde.annotation.Serdeable
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Serdeable
data class Completion(
    val id: Int? = null,
    val taskId: Int,
    val completionTimestamp: Instant,
    val dueDateWhenCompleted: LocalDate,
    val wasAutocomplete: Boolean
)

data class CompletionInput(
    val trigger: Trigger,
    val currentDueDate: LocalDate
)
