package com.timsummertonbrier.chores.database

import com.timsummertonbrier.chores.domain.*
import jakarta.inject.Singleton
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder

object Tasks : IntIdTable("task") {
    var name = text("name")
    var description = text("description").nullable()
    var dueDate = date("due_date").nullable()
    var autocomplete = bool("autocomplete")
    var createdTimestamp = timestamp("created_timestamp")
    var updatedTimestamp = timestamp("updated_timestamp")

    fun UpdateBuilder<Int>.populateFrom(taskRequest: TaskRequest) {
        this[name] = taskRequest.name!!
        this[description] = taskRequest.description
        this[dueDate] = taskRequest.dueDate
        this[autocomplete] = taskRequest.autocomplete
    }
}

object TaskTriggers : IntIdTable("task_trigger") {
    var taskId = reference("task_id", Tasks.id)
    var triggerType = enumerationByName<TriggerType>("trigger_type", 30)
    var daysBetween = integer("days_between").nullable()
    var dayOfWeek = integer("day_of_week").nullable()
    var dayOfMonth = integer("day_of_month").nullable()
    var monthOfYear = integer("month_of_year").nullable()

    fun UpdateBuilder<Int>.populateFrom(triggerRequest: TriggerRequest, taskId: Int) {
        this[this@TaskTriggers.taskId] = taskId
        this[triggerType] = triggerRequest.triggerType!!
        when(triggerRequest.triggerType) {
            TriggerType.FIXED_DELAY -> this[daysBetween] = triggerRequest.daysBetween
            TriggerType.WEEKLY -> this[dayOfWeek] = triggerRequest.dayOfWeek
            TriggerType.MONTHLY -> this[dayOfMonth] = triggerRequest.dayOfMonth
            TriggerType.YEARLY -> {
                this[monthOfYear] = triggerRequest.monthOfYear
                this[dayOfMonth] = triggerRequest.dayOfMonth
            }
            TriggerType.ONE_OFF -> {}
        }
    }
}

fun ResultRow.toTask(): Task {
    return Task(
        this[Tasks.id].value,
        this[Tasks.name],
        this[Tasks.description],
        this[Tasks.dueDate],
        this[Tasks.autocomplete],
        this[Tasks.createdTimestamp],
        this[Tasks.updatedTimestamp],
        this.toTrigger()
    )
}

fun ResultRow.toTrigger(): Trigger {
    return when (this[TaskTriggers.triggerType]) {
        TriggerType.FIXED_DELAY -> FixedDelayTrigger(this[TaskTriggers.daysBetween]!!)
        TriggerType.WEEKLY -> WeeklyTrigger(this[TaskTriggers.dayOfWeek]!!)
        TriggerType.MONTHLY -> MonthlyTrigger(this[TaskTriggers.dayOfMonth]!!)
        TriggerType.YEARLY -> YearlyTrigger(this[TaskTriggers.monthOfYear]!!, this[TaskTriggers.dayOfMonth]!!)
        TriggerType.ONE_OFF -> OneOffTrigger()
    }
}

@Singleton
@ExposedTransactional
class TaskRepository {
    fun addTask(taskRequest: TaskRequest): Int {
        val taskId = Tasks.insertAndGetId { it.populateFrom(taskRequest) }.value
        TaskTriggers.insert { it.populateFrom(taskRequest.trigger, taskId) }
        return taskId
    }

    fun findById(id: Int): Task {
        return (Tasks innerJoin TaskTriggers).select { Tasks.id eq id }.map { it.toTask() }.first()
    }
}