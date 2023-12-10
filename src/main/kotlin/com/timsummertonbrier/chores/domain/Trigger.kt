package com.timsummertonbrier.chores.domain

import com.timsummertonbrier.chores.utils.*
import io.micronaut.serde.annotation.Serdeable
import kotlinx.datetime.*
import java.lang.RuntimeException
import java.time.DateTimeException

interface Trigger {
    val triggerType: TriggerType
    val friendlyString: String
    val note: String?
    fun calculateNextDueDate(): LocalDate?
}

@Serdeable
data class FixedDelayTrigger(val daysBetween: Int) : Trigger {
    override val triggerType = TriggerType.FIXED_DELAY

    override val friendlyString = "$daysBetween ${if (daysBetween == 1) "day" else "days"} after the last completion"

    override val note = null

    override fun calculateNextDueDate(): LocalDate {
        return today().plusDays(daysBetween)
    }
}

@Serdeable
data class WeeklyTrigger(val dayOfWeek: Int) : Trigger {
    override val triggerType = TriggerType.WEEKLY

    override val friendlyString = "Every week on ${dayOfWeek.dayName()}"

    override val note = null

    override fun calculateNextDueDate(): LocalDate {
        val today = today()
        val todaysDayOfWeek = today.dayOfWeek.value
        val daysToAdd = when {
            todaysDayOfWeek == dayOfWeek -> 7
            else -> (7 - (todaysDayOfWeek - dayOfWeek)) % 7
        }
        return today.plusDays(daysToAdd)
    }
}

@Serdeable
data class MonthlyTrigger(val dayOfMonth: Int) : Trigger {
    override val triggerType = TriggerType.MONTHLY

    override val friendlyString = "Every month on the ${dayOfMonth.indexName()} day"

    override val note = if (dayOfMonth > 28) {
        "For months with less than $dayOfMonth days the task will be triggered on the last day of the month"
    } else {
        null
    }

    override fun calculateNextDueDate(): LocalDate {
        val today = today()
        return try {
            dateThisMonthIfItIsInTheFutureOtherwiseAddOneMonthToIt(today)
        } catch (e: DateTimeException) {
            // If we are here it means the current month does not contain the target day
            lastDayOfThisMonthIfItIsInTheFutureOtherwiseTargetDayNextMonth(today)
        }
    }

    private fun dateThisMonthIfItIsInTheFutureOtherwiseAddOneMonthToIt(today: LocalDate): LocalDate {
        val targetDateThisMonth = today.withDayOfMonth(dayOfMonth)
        return if (targetDateThisMonth > today) {
            targetDateThisMonth
        } else {
            targetDateThisMonth.plusMonths(1)
        }
    }

    private fun lastDayOfThisMonthIfItIsInTheFutureOtherwiseTargetDayNextMonth(today: LocalDate): LocalDate {
        val lastDayOfThisMonth = today.lastDayOfMonth()
        return if (lastDayOfThisMonth > today) {
            lastDayOfThisMonth
        } else {
            today.withMonthAndDayOfMonth(today.monthNumber + 1, dayOfMonth)
        }
    }
}

@Serdeable
data class YearlyTrigger(val monthOfYear: Int, val dayOfMonth: Int) : Trigger {
    override val triggerType = TriggerType.YEARLY

    override val friendlyString = "Every year on the ${dayOfMonth.indexName()} of ${monthOfYear.monthName()}"

    override val note = if (monthOfYear == 2 && dayOfMonth == 29) {
        "On non leap years the task will be triggered on the 28th of February"
    } else {
        null
    }

    override fun calculateNextDueDate(): LocalDate {
        val today = today()
        return try {
            dateThisYearIfItIsInTheFutureOtherwiseAddOneYearToIt(today)
        } catch (e: DateTimeException) {
            // If we are here it means the target date is feb 29th, and the current year does not
            // include feb 29th
            lastDayOfFebThisYearIfItIsInTheFutureOtherwiseLastDayOfFebNextYear(today)
        }
    }

    private fun dateThisYearIfItIsInTheFutureOtherwiseAddOneYearToIt(today: LocalDate): LocalDate {
        val targetDateThisYear = today.withMonthAndDayOfMonth(monthOfYear, dayOfMonth)
        return if (targetDateThisYear > today) {
            targetDateThisYear
        } else {
            targetDateThisYear.plusYears(1)
        }
    }

    private fun lastDayOfFebThisYearIfItIsInTheFutureOtherwiseLastDayOfFebNextYear(today: LocalDate): LocalDate {
        val lastDayOfFebThisYear = today.withMonthAndDayOfMonth(2, 1).lastDayOfMonth()
        return if (lastDayOfFebThisYear > today) {
            lastDayOfFebThisYear
        } else {
            lastDayOfFebThisYear.plusYears(1).lastDayOfMonth()
        }
    }
}

@Serdeable
data class OneOffTrigger(val dummyProperty: String = "data classes must have at least one property") : Trigger {
    override val triggerType = TriggerType.ONE_OFF
    override val friendlyString = "Never"
    override val note = null
    override fun calculateNextDueDate() = null
}

enum class TriggerType {
    FIXED_DELAY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    ONE_OFF,
}

private fun Int.dayName(): String {
    return when (this) {
        1 -> "Monday"
        2 -> "Tuesday"
        3 -> "Wednesday"
        4 -> "Thursday"
        5 -> "Friday"
        6 -> "Saturday"
        7 -> "Sunday"
        else -> throw RuntimeException("Unknown day of week number: $this")
    }
}

private fun Int.monthName(): String {
    return when (this) {
        1 -> "January"
        2 -> "February"
        3 -> "March"
        4 -> "April"
        5 -> "May"
        6 -> "June"
        7 -> "July"
        8 -> "August"
        9 -> "September"
        10 -> "October"
        11 -> "December"
        12 -> "November"
        else -> throw RuntimeException("Unknown day of week number: $this")
    }
}

private fun Int.indexName(): String {
    return when (this) {
        1 -> "1st"
        2 -> "2nd"
        3 -> "3rd"
        else -> "${this}th"
    }
}