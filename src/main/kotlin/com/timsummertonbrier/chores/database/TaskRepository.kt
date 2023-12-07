package com.timsummertonbrier.chores.database

import com.timsummertonbrier.chores.domain.*
import com.timsummertonbrier.chores.utils.atStartOfDay
import com.timsummertonbrier.chores.utils.now
import com.timsummertonbrier.chores.utils.today
import jakarta.inject.Singleton
import kotlinx.datetime.*
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import kotlin.time.Duration.Companion.days

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
        this[Tasks.dueDate]!!.daysUntil(today()),
    )
}

fun ResultRow.toCompletedTodayTasksTaskView(): CompletedTodayTasksTaskView {
    return CompletedTodayTasksTaskView(
        this[Tasks.id].value,
        this[Tasks.name]
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

    fun updateDueDate(taskId: Int, newDueDate: LocalDate?) {
        Tasks.update({ Tasks.id eq taskId }) { it[dueDate] = newDueDate }
    }

    fun deleteTask(id: Int) {
        Tasks.deleteWhere { Tasks.id eq id }
    }

    fun findById(id: Int): Task {
        return (Tasks innerJoin TaskTriggers)
            .slice(
                Tasks.id,
                Tasks.name,
                Tasks.description,
                Tasks.dueDate,
                Tasks.autocomplete,
                Tasks.createdTimestamp,
                Tasks.updatedTimestamp,
                TaskTriggers.triggerType,
                TaskTriggers.daysBetween,
                TaskTriggers.dayOfWeek,
                TaskTriggers.dayOfMonth,
                TaskTriggers.monthOfYear
            )
            .select { Tasks.id eq id }
            .limit(1)
            .map { it.toTask() }
            .first()
    }

    fun getAllTasksForAllTasksPage(): List<AllTasksTaskView> {
        return Tasks
            .slice(Tasks.id, Tasks.name, Tasks.dueDate)
            .selectAll()
            .map { it.toAllTasksTaskView() }
    }

    fun getOverdueTasksForHomePage(): List<OverdueTasksTaskView> {
        return Tasks
            .slice(Tasks.id, Tasks.name, Tasks.dueDate)
            .select { Tasks.dueDate lessEq today() }
            .map { it.toOverdueTasksTaskView() }
    }

    fun getCompletedTodayTasksForHomePage(): List<CompletedTodayTasksTaskView> {
        return (TaskCompletions innerJoin Tasks)
            .slice(TaskCompletions.id, Tasks.id, Tasks.name)
            .select { wasCompletedToday() and wasNotAutocomplete() }
            .map { it.toCompletedTodayTasksTaskView() }
    }

    fun getOverdueAutocompleteTaskIds(): List<Int> {
        return Tasks
            .slice(Tasks.id)
            .select { Tasks.dueDate lessEq today() and Tasks.autocomplete eq Op.TRUE }
            .map { it[Tasks.id].value }
    }

    private fun wasCompletedToday(): Op<Boolean> {
        val startOfToday = now().atStartOfDay()
        val startOfTomorrow = startOfToday.plus(1.days)

        return (TaskCompletions.completionTimestamp greaterEq startOfToday) and (TaskCompletions.completionTimestamp less startOfTomorrow)
    }

    private fun wasNotAutocomplete(): Op<Boolean> {
        return TaskCompletions.wasAutocomplete eq Op.FALSE
    }
}