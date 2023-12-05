package com.timsummertonbrier.chores.database

import com.timsummertonbrier.chores.domain.TriggerType
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object Tasks : IntIdTable("task") {
    var name = text("name")
    var description = text("description").nullable()
    var dueDate = date("due_date").nullable()
    var autocomplete = bool("autocomplete")
    var createdTimestamp = timestamp("created_timestamp")
    var updatedTimestamp = timestamp("updated_timestamp")
}

object TaskTriggers : IntIdTable("task_trigger") {
    var task_id = reference("task_id", Tasks.id)
    var type = enumerationByName<TriggerType>("type", 30)
    var daysBetween = integer("days_between").nullable()
    var dayOfWeek = integer("day_of_week").nullable()
    var dayOfMonth = integer("day_of_month").nullable()
    var monthOfYear = integer("month_of_year").nullable()
}
