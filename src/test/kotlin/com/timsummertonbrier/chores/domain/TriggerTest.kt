package com.timsummertonbrier.chores.domain

import com.timsummertonbrier.chores.utils.today
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.datetime.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TriggerTest {

    @BeforeEach
    fun beforeEach() {
        mockkStatic(::today)
    }

    @AfterEach
    fun afterEach() {
        unmockkAll()
    }

    @Nested
    inner class FixedDelay {
        @Test
        fun `fixed delay triggers add daysBetween to today`() {
            todayIs("2020-11-06")
            val trigger = FixedDelayTrigger(daysBetween = 90)
            trigger.assertNextDueDateIs("2021-02-04")
        }

        @Test
        fun `fixed delay triggers handle leap years`() {
            todayIs("2020-02-25")
            val trigger = FixedDelayTrigger(daysBetween = 10)
            trigger.assertNextDueDateIs("2020-03-06")
        }
    }

    @Nested
    inner class Weekly {
        @Test
        fun `weekly triggers choose day this week if it is in the future`() {
            todayIs("2020-12-29")
            val trigger = WeeklyTrigger(dayOfWeek = 6)
            trigger.assertNextDueDateIs("2021-01-02")
        }

        @Test
        fun `weekly triggers choose day next week if day this week is in the past`() {
            todayIs("2020-12-30")
            val trigger = WeeklyTrigger(dayOfWeek = 1)
            trigger.assertNextDueDateIs("2021-01-04")
        }

        @Test
        fun `weekly triggers choose day next week if day this week is today`() {
            todayIs("2020-12-30")
            val trigger = WeeklyTrigger(dayOfWeek = 3)
            trigger.assertNextDueDateIs("2021-01-06")
        }

        @Test
        fun `weekly triggers handle leap years`() {
            todayIs("2020-02-28")
            val trigger = WeeklyTrigger(dayOfWeek = 2)
            trigger.assertNextDueDateIs("2020-03-03")
        }
    }

    @Nested
    inner class Monthly {
        @Test
        fun `monthly triggers choose day this month if it is in the future`() {
            todayIs("2020-12-15")
            val trigger = MonthlyTrigger(dayOfMonth = 16)
            trigger.assertNextDueDateIs("2020-12-16")
        }

        @Test
        fun `monthly triggers choose day next month if day this month is in the past`() {
            todayIs("2020-12-15")
            val trigger = MonthlyTrigger(dayOfMonth = 14)
            trigger.assertNextDueDateIs("2021-01-14")
        }

        @Test
        fun `monthly triggers choose day next month if day this month is today`() {
            todayIs("2020-12-15")
            val trigger = MonthlyTrigger(dayOfMonth = 15)
            trigger.assertNextDueDateIs("2021-01-15")
        }

        @Test
        fun `monthly triggers handle leap years`() {
            todayIs("2020-02-15")
            val trigger = MonthlyTrigger(dayOfMonth = 29)
            trigger.assertNextDueDateIs("2020-02-29")
        }

        @Test
        fun `if the target day does not exist this month, choose last day of this month`() {
            todayIs("2020-02-15")
            val trigger = MonthlyTrigger(dayOfMonth = 30)
            trigger.assertNextDueDateIs("2020-02-29")
        }

        @Test
        fun `if the target day does not exist this month, and we are on the last day of the month, choose target day next month`() {
            todayIs("2020-02-29")
            val trigger = MonthlyTrigger(dayOfMonth = 30)
            trigger.assertNextDueDateIs("2020-03-30")
        }

        @Test
        fun `if the target day is in the past for this month, and does not exist next month, choose last day of next month`() {
            todayIs("2020-01-31")
            val trigger = MonthlyTrigger(dayOfMonth = 30)
            trigger.assertNextDueDateIs("2020-02-29")
        }
    }

    @Nested
    inner class Yearly {
        @Test
        fun `yearly triggers choose date this year if it is in the future`() {
            todayIs("2020-03-15")
            val trigger = YearlyTrigger(monthOfYear = 3, dayOfMonth = 16)
            trigger.assertNextDueDateIs("2020-03-16")
        }

        @Test
        fun `yearly triggers choose date next year if date this year is in the past`() {
            todayIs("2020-12-15")
            val trigger = YearlyTrigger(monthOfYear = 12, dayOfMonth = 14)
            trigger.assertNextDueDateIs("2021-12-14")
        }

        @Test
        fun `yearly triggers choose date next year if date this year is today`() {
            todayIs("2020-12-15")
            val trigger = YearlyTrigger(monthOfYear = 12, dayOfMonth = 15)
            trigger.assertNextDueDateIs("2021-12-15")
        }

        @Test
        fun `when next due date is feb 29th, and that is in the future for this year, and it is a leap year, choose feb 29th`() {
            todayIs("2020-02-27")
            val trigger = YearlyTrigger(monthOfYear = 2, dayOfMonth = 29)
            trigger.assertNextDueDateIs("2020-02-29")
        }

        @Test
        fun `when next due date is feb 29th, and that is in the future for this year, and it is not a leap year, choose feb 28th`() {
            todayIs("2021-02-27")
            val trigger = YearlyTrigger(monthOfYear = 2, dayOfMonth = 29)
            trigger.assertNextDueDateIs("2021-02-28")
        }

        @Test
        fun `when next due date is feb 29th, and that is in the past for this year, and next year is a leap year, choose feb 29th`() {
            todayIs("2019-03-01")
            val trigger = YearlyTrigger(monthOfYear = 2, dayOfMonth = 29)
            trigger.assertNextDueDateIs("2020-02-29")
        }

        @Test
        fun `when next due date is feb 29th, and that is in the past for this year, and next is not a leap year, choose feb 28th`() {
            todayIs("2021-03-01")
            val trigger = YearlyTrigger(monthOfYear = 2, dayOfMonth = 29)
            trigger.assertNextDueDateIs("2022-02-28")
        }

        @Test
        fun `when next due date is feb 29th, and it is currently feb 28th, and it is a leap year, choose feb 29th this year`() {
            todayIs("2020-02-28")
            val trigger = YearlyTrigger(monthOfYear = 2, dayOfMonth = 29)
            trigger.assertNextDueDateIs("2020-02-29")
        }

        @Test
        fun `when next due date is feb 29th, and it is currently feb 28th, and it is not a leap year, and next year is not a leap year, choose feb 28th next year`() {
            todayIs("2021-02-28")
            val trigger = YearlyTrigger(monthOfYear = 2, dayOfMonth = 29)
            trigger.assertNextDueDateIs("2022-02-28")
        }

        @Test
        fun `when next due date is feb 29th, and it is currently feb 28th, and it is not a leap year, and next year is a leap year, choose feb 29th next year`() {
            todayIs("2019-02-28")
            val trigger = YearlyTrigger(monthOfYear = 2, dayOfMonth = 29)
            trigger.assertNextDueDateIs("2020-02-29")
        }
    }

    @Nested inner class OneOff {
        @Test
        fun `one off triggers return null`() {
            OneOffTrigger().assertNextDueDateIsNull()
        }
    }

    private fun todayIs(isoDateString: String) {
        every { today() } returns LocalDate.parse(isoDateString)
    }

    private fun Trigger.assertNextDueDateIs(isoDateString: String) {
        assertThat(calculateNextDueDate()).isEqualTo(LocalDate.parse(isoDateString))
    }

    private fun Trigger.assertNextDueDateIsNull() {
        assertThat(calculateNextDueDate()).isNull()
    }
}