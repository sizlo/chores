package com.timsummertonbrier.chores.database

import com.timsummertonbrier.chores.domain.*
import jakarta.inject.Singleton
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
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

object RevertedTaskCompletions : IntIdTable("reverted_task_completion") {
    var taskCompletionId = integer("task_completion_id")
    var taskId = reference("task_id", Tasks.id)
    var completionTimestamp = timestamp("completion_timestamp")
    var dueDateWhenCompleted = date("due_date_when_completed")
    var wasAutocomplete = bool("was_autocomplete")

    fun UpdateBuilder<Int>.populateFrom(completion: Completion) {
        this[taskCompletionId] = completion.id!!
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

fun ResultRow.toCompletion(): Completion {
    return Completion(
        id = this[TaskCompletions.id].value,
        taskId = this[TaskCompletions.taskId].value,
        completionTimestamp = this[TaskCompletions.completionTimestamp],
        dueDateWhenCompleted = this[TaskCompletions.dueDateWhenCompleted],
        wasAutocomplete = this[TaskCompletions.wasAutocomplete]
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
            .limit(1)
            .map { it.toCompletionInput() }
            .first()
    }

    fun findLatestNonAutoCompletionForTaskId(taskId: Int): Completion {
        return TaskCompletions
            .slice(
                TaskCompletions.id,
                TaskCompletions.taskId,
                TaskCompletions.completionTimestamp,
                TaskCompletions.dueDateWhenCompleted,
                TaskCompletions.wasAutocomplete
            )
            .select { TaskCompletions.taskId eq taskId and TaskCompletions.wasAutocomplete eq Op.FALSE }
            .orderBy(TaskCompletions.completionTimestamp, SortOrder.DESC)
            .limit(1)
            .map { it.toCompletion() }
            .first()
    }

    fun addCompletion(completion: Completion): Int {
        return TaskCompletions.insertAndGetId { it.populateFrom(completion) }.value
    }

    fun deleteCompletion(id: Int) {
        TaskCompletions.deleteWhere { TaskCompletions.id eq id }
    }

    fun addRevertedCompletion(completion: Completion) {
        RevertedTaskCompletions.insert { it.populateFrom(completion) }
    }

}