package com.timsummertonbrier.chores.domain

import com.timsummertonbrier.chores.utils.lastDayOfMonth
import com.timsummertonbrier.chores.utils.today
import com.timsummertonbrier.chores.utils.withDayOfMonth
import com.timsummertonbrier.chores.utils.withMonthAndDayOfMonth
import io.micronaut.serde.annotation.Serdeable
import kotlinx.datetime.*
import java.time.DateTimeException

interface Trigger {
    val triggerType: TriggerType
    fun calculateNextDueDate(): LocalDate?
}

@Serdeable
data class FixedDelayTrigger(val daysBetween: Int) : Trigger {
    override val triggerType = TriggerType.FIXED_DELAY

    override fun calculateNextDueDate(): LocalDate {
        return today().plus(DatePeriod(days = daysBetween))
    }
}

@Serdeable
data class WeeklyTrigger(val dayOfWeek: Int) : Trigger {
    override val triggerType = TriggerType.WEEKLY

    override fun calculateNextDueDate(): LocalDate {
        val today = today()
        val todaysDayOfWeek = today.dayOfWeek.value
        val daysToAdd = when {
            todaysDayOfWeek == dayOfWeek -> 7
            else -> (7 - (todaysDayOfWeek - dayOfWeek)) % 7
        }
        return today.plus(DatePeriod(days = daysToAdd))
    }
}

@Serdeable
data class MonthlyTrigger(val dayOfMonth: Int) : Trigger {
    override val triggerType = TriggerType.MONTHLY

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
            targetDateThisMonth.plus(DatePeriod(months = 1))
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
            targetDateThisYear.plus(DatePeriod(years = 1))
        }
    }

    private fun lastDayOfFebThisYearIfItIsInTheFutureOtherwiseLastDayOfFebNextYear(today: LocalDate): LocalDate {
        val lastDayOfFebThisYear = today.withMonthAndDayOfMonth(2, 1).lastDayOfMonth()
        return if (lastDayOfFebThisYear > today) {
            lastDayOfFebThisYear
        } else {
            lastDayOfFebThisYear.plus(DatePeriod(years = 1)).lastDayOfMonth()
        }
    }
}

@Serdeable
data class OneOffTrigger(val dummyProperty: String = "data classes must have at least one property") : Trigger {
    override val triggerType = TriggerType.ONE_OFF

    override fun calculateNextDueDate() = null
}

enum class TriggerType {
    FIXED_DELAY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    ONE_OFF,
}