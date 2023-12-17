package com.timsummertonbrier.chores.database

import com.timsummertonbrier.chores.domain.Completion
import com.timsummertonbrier.chores.domain.TaskRequest
import com.timsummertonbrier.chores.domain.TriggerType
import com.timsummertonbrier.chores.utils.minusDays
import com.timsummertonbrier.chores.utils.now
import com.timsummertonbrier.chores.utils.plusDays
import com.timsummertonbrier.chores.utils.today
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

@MicronautTest
class TaskRepositoryTest {

    @Inject
    private lateinit var testSubject: TaskRepository

    @Inject
    private lateinit var taskCompletionRepository: TaskCompletionRepository

    private var taskIds = mutableListOf<Int>()

    @AfterEach
    fun afterEach() {
        taskIds.forEach { testSubject.deleteTask(it) }
        taskIds = mutableListOf()
    }

    @Test
    fun `gets correct overdue tasks for homepage`() {
        transaction {
            // Ignored because due tomorrow
            addTask(dueDate = today().plusDays(1))
            // Included
            val dueToday = addTask(dueDate = today())
            // Included
            val dueYesterday = addTask(dueDate = today().minusDays(1))

            val actual = testSubject.getOverdueTasksForHomePage()

            assertThat(actual.map { it.id }).containsExactlyInAnyOrder(dueToday, dueYesterday)
        }
    }

    @Test
    fun `gets correct completed today tasks for homepage`() {
        transaction {
            val today = now()

            // Ignored because autocomplete
            val autocompleted = addTask()
            addCompletion(taskId = autocompleted, completionTimestamp = today, wasAutocomplete = true)
            // Ignored because completed in the past
            val completedInThePast = addTask()
            addCompletion(taskId = completedInThePast, completionTimestamp = today.minusDays(1), wasAutocomplete = false)
            // Included
            val expected1 = addTask()
            addCompletion(taskId = expected1, completionTimestamp = today, wasAutocomplete = false)
            // Ignored because completed in the future
            val completedInTheFuture = addTask()
            addCompletion(taskId = completedInTheFuture, completionTimestamp = today.minusDays(1), wasAutocomplete = false)
            // Included
            val expected2 = addTask()
            addCompletion(taskId = expected2, completionTimestamp = today, wasAutocomplete = false)

            val actual = testSubject.getCompletedTodayTasksForHomePage()

            assertThat(actual.map { it.id }).containsExactlyInAnyOrder(expected1, expected2)
        }
    }

    @Test
    fun `gets correct tasks for autocompletion`() {
        transaction {
            // Ignored because not autocomplete
            addTask(dueDate = today().minusDays(1), autocomplete = false)
            // Ignored because not due
            addTask(dueDate = today().plusDays(1), autocomplete = true)
            // Ignored because due today
            addTask(dueDate = today(), autocomplete = true)
            // Included
            val expected1 = addTask(dueDate = today().minusDays(1), autocomplete = true)
            // Included
            val expected2 = addTask(dueDate = today().minusDays(2), autocomplete = true)

            val actual = testSubject.getTasksForAutocompletion()

            assertThat(actual).containsExactlyInAnyOrder(expected1, expected2)
        }
    }

    private fun addTask(dueDate: LocalDate = today(), autocomplete: Boolean = false): Int {
        val id = testSubject.addTask(
            TaskRequest(
                name = "test task",
                dueDate = dueDate.toString(),
                autocomplete = autocomplete,
                triggerType = TriggerType.FIXED_DELAY.name,
                daysBetween = "10"
            )
        )
        taskIds.add(id)
        return id
    }

    private fun addCompletion(taskId: Int, completionTimestamp: Instant, wasAutocomplete: Boolean) {
        taskCompletionRepository.addCompletion(
            Completion(
                taskId = taskId,
                completionTimestamp = completionTimestamp,
                dueDateWhenCompleted = today(),
                wasAutocomplete = wasAutocomplete
            )
        )
    }
}