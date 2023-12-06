package com.timsummertonbrier.chores.database

import com.timsummertonbrier.chores.domain.*
import com.timsummertonbrier.chores.utils.today
import jakarta.inject.Singleton
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
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
        this[dueDate] = taskRequest.dueDateAsDate()
        this[autocomplete] = taskRequest.autocomplete!!
    }
}

object TaskTriggers : Table("task_trigger") {
    var taskId = reference("task_id", Tasks.id)
    var triggerType = enumerationByName<TriggerType>("trigger_type", 30)
    var daysBetween = integer("days_between").nullable()
    var dayOfWeek = integer("day_of_week").nullable()
    var dayOfMonth = integer("day_of_month").nullable()
    var monthOfYear = integer("month_of_year").nullable()

    fun UpdateBuilder<Int>.populateFrom(taskRequest: TaskRequest, taskId: Int) {
        this[this@TaskTriggers.taskId] = taskId
        this[triggerType] = taskRequest.triggerTypeAsEnum()
        nullOutParameters()
        setParametersForType(taskRequest)
    }

    private fun UpdateBuilder<Int>.setParametersForType(taskRequest: TaskRequest) {
        when (taskRequest.triggerTypeAsEnum()) {
            TriggerType.FIXED_DELAY -> this[daysBetween] = taskRequest.daysBetween!!.toInt()
            TriggerType.WEEKLY -> this[dayOfWeek] = taskRequest.dayOfWeek!!.toInt()
            TriggerType.MONTHLY -> this[dayOfMonth] = taskRequest.dayOfMonth!!.toInt()
            TriggerType.YEARLY -> {
                this[monthOfYear] = taskRequest.monthOfYear!!.toInt()
                this[dayOfMonth] = taskRequest.dayOfMonth!!.toInt()
            }
            TriggerType.ONE_OFF -> {}
        }
    }

    private fun UpdateBuilder<Int>.nullOutParameters() {
        this[daysBetween] = null
        this[dayOfWeek] = null
        this[dayOfMonth] = null
        this[monthOfYear] = null
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
        this.toTrigger(),
    )
}

fun ResultRow.toAllTasksTaskView(): AllTasksTaskView {
    return AllTasksTaskView(
        this[Tasks.id].value,
        this[Tasks.name],
        this[Tasks.dueDate],
    )
}

fun ResultRow.toOverdueTasksTaskView(): OverdueTasksTaskView {
    return OverdueTasksTaskView(
        this[Tasks.id].value,
        this[Tasks.name],
        this[Tasks.dueDate]!!.daysUntil(LocalDate.today()),
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
        TaskTriggers.insert { it.populateFrom(taskRequest, taskId) }
        return taskId
    }

    fun updateTask(taskId: Int, taskRequest: TaskRequest) {
        Tasks.update({ Tasks.id eq taskId }) { it.populateFrom(taskRequest) }
        TaskTriggers.update({ TaskTriggers.taskId eq taskId }) { it.populateFrom(taskRequest, taskId) }
    }

    fun deleteTask(id: Int) {
        Tasks.deleteWhere { Tasks.id eq id }
    }

    fun findById(id: Int): Task {
        return (Tasks innerJoin TaskTriggers).select { Tasks.id eq id }.map { it.toTask() }.first()
    }

    fun getAllTasksForAllTasksPage(): List<AllTasksTaskView> {
        return Tasks.slice(Tasks.id, Tasks.name, Tasks.dueDate).selectAll().map { it.toAllTasksTaskView() }
    }

    fun getOverdueTasksForHomePage(): List<OverdueTasksTaskView> {
        return Tasks.slice(Tasks.id, Tasks.name, Tasks.dueDate).select { Tasks.dueDate lessEq LocalDate.today() }.map { it.toOverdueTasksTaskView() }
    }
}