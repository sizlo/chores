package com.timsummertonbrier.chores.utils

import kotlinx.datetime.*
import java.lang.RuntimeException
import java.time.DateTimeException
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.days

fun today(): LocalDate {
    return Clock.System.todayIn(TimeZone.currentSystemDefault())
}

fun now(): Instant {
    return Clock.System.now()
}

fun LocalDate.plusDays(days: Int): LocalDate {
    return this.plus(DatePeriod(days = days))
}

fun LocalDate.minusDays(days: Int): LocalDate {
    return this.minus(DatePeriod(days = days))
}

fun LocalDate.plusMonths(months: Int): LocalDate {
    return this.plus(DatePeriod(months = months))
}

fun LocalDate.plusYears(years: Int): LocalDate {
    return this.plus(DatePeriod(years = years))
}

fun LocalDate.withDayOfMonth(dayOfMonth: Int): LocalDate {
    return this.toJavaLocalDate().withDayOfMonth(dayOfMonth).toKotlinLocalDate()
}

fun LocalDate.withMonthAndDayOfMonth(month: Int, dayOfMonth: Int): LocalDate {
    try {
        val isoDateString = "${this.year}-${addLeadingZero(month)}-${addLeadingZero(dayOfMonth)}"
        return LocalDate.parse(isoDateString)
    } catch (e: IllegalArgumentException) {
        // Translate IllegalArgumentException to DateTimeException
        throw DateTimeException(e.message, e)
    }
}

fun LocalDate.lastDayOfMonth(): LocalDate {
    return this.plusMonths(1).withDayOfMonth(1).minusDays(1)
}

fun Instant.plusDays(days: Int): Instant {
    return this.plus(days.days)
}

fun Instant.minusDays(days: Int): Instant {
    return this.minus(days.days)
}

fun Instant.atStartOfDay(): Instant {
    return this.toJavaInstant().truncatedTo(ChronoUnit.DAYS).toKotlinInstant()
}

fun Int.dayName(): String {
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

fun Int.monthName(): String {
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
        11 -> "November"
        12 -> "December"
        else -> throw RuntimeException("Unknown day of week number: $this")
    }
}

fun Int.indexName(): String {
    return when (this) {
        1 -> "1st"
        2 -> "2nd"
        3 -> "3rd"
        else -> "${this}th"
    }
}

private fun addLeadingZero(num: Int): String {
    return if (num >= 10 ) {
        num.toString()
    } else {
        "0$num"
    }
}
