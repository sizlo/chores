package com.timsummertonbrier.chores.database

import com.timsummertonbrier.chores.domain.*
import jakarta.inject.Singleton
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder

object TaskCompletions : IntIdTable("task_completion") {
    var taskId = reference("task_id", Tasks.id)
    var completionTimestamp = timestamp("completion_timestamp")
    var dueDateWhenCompleted = date("due_date_when_completed")
    var wasAutocomplete = bool("was_autocomplete")

    fun UpdateBuilder<Int>.populateFrom(completion: Completion) {
        this[taskId] = completion.taskId
        this[completionTimestamp] = completion.completionTimestamp
        this[dueDateWhenCompleted] = completion.dueDateWhenCompleted
        this[wasAutocomplete] = completion.wasAutocomplete
    }
}

fun ResultRow.toCompletionInput(): CompletionInput {
    return CompletionInput(
        trigger = toTrigger(),
        currentDueDate = this[Tasks.dueDate]!!
    )
}

@Singleton
class TaskCompletionRepository {

    fun findCompletionInputByTaskId(taskId: Int): CompletionInput {
        return (Tasks innerJoin TaskTriggers)
            .slice(
                Tasks.dueDate,
                TaskTriggers.triggerType,
                TaskTriggers.daysBetween,
                TaskTriggers.dayOfWeek,
                TaskTriggers.dayOfMonth,
                TaskTriggers.monthOfYear
            )
            .select { Tasks.id eq taskId }
            .map { it.toCompletionInput() }
            .first()
    }

    fun addCompletion(completion: Completion) {
        TaskCompletions.insert { it.populateFrom(completion) }
    }
}