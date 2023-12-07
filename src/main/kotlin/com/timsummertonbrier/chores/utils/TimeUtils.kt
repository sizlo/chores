package com.timsummertonbrier.chores.utils

import kotlinx.datetime.*
import java.time.DateTimeException

fun today(): LocalDate {
    return Clock.System.todayIn(TimeZone.currentSystemDefault())
}

fun now(): Instant {
    return Clock.System.now()
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
    return this.plus(DatePeriod(months = 1)).withDayOfMonth(1).minus(DatePeriod(days = 1))
}

fun addLeadingZero(num: Int): String {
    return if (num >= 10 ) {
        num.toString()
    } else {
        "0$num"
    }
}
