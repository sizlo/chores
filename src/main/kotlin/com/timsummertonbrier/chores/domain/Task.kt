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

@Serdeable
data class TaskRequest(
    val name: String? = null,
    val description: String? = null,
    val dueDate: LocalDate? = null,
    val autocomplete: Boolean = false,
    val trigger: TriggerRequest = TriggerRequest(),
)
