package com.timsummertonbrier.chores.database

import com.timsummertonbrier.chores.domain.Completion
import com.timsummertonbrier.chores.domain.TaskRequest
import com.timsummertonbrier.chores.domain.TriggerType
import com.timsummertonbrier.chores.utils.now
import com.timsummertonbrier.chores.utils.plusDays
import com.timsummertonbrier.chores.utils.today
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import kotlinx.datetime.Instant
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

@MicronautTest
class TaskCompletionRepositoryTest {

    @Inject
    private lateinit var taskRepository: TaskRepository

    @Inject
    private lateinit var testSubject: TaskCompletionRepository

    private var taskId: Int? = null
    private var otherTaskId: Int? = null

    @AfterEach
    fun afterEach() {
        taskId?.let { taskRepository.deleteTask(it) }
        otherTaskId?.let { taskRepository.deleteTask(it) }
        taskId = null
        otherTaskId = null
    }

    @Test
    fun `selects latest non autocomplete completion`() {
        transaction {
            taskId = addTask()
            otherTaskId = addTask()

            val base = now()

            // Ignore because not latest
            addCompletion(taskId = taskId!!, completionTimestamp = base.plusDays(1), wasAutocomplete = false)
            // This one should be selected
            val expected = addCompletion(taskId = taskId!!, completionTimestamp = base.plusDays(2), wasAutocomplete = false)
            // Ignored because not latest
            addCompletion(taskId = taskId!!, completionTimestamp = base.plusDays(1), wasAutocomplete = false)
            // Ignored because autocomplete
            addCompletion(taskId = taskId!!, completionTimestamp = base.plusDays(3), wasAutocomplete = true)
            // Ignored because wrong task ID
            addCompletion(taskId = otherTaskId!!, completionTimestamp = base.plusDays(3), wasAutocomplete = false)

            val actual = testSubject.findLatestNonAutoCompletionForTaskId(taskId!!)

            assertThat(actual.id).isEqualTo(expected)
        }
    }

    private fun addTask(): Int {
        return taskRepository.addTask(
            TaskRequest(
                name = "test task",
                autocomplete = false,
                triggerType = TriggerType.FIXED_DELAY.name,
                daysBetween = "10"
            )
        )
    }

    private fun addCompletion(taskId: Int, completionTimestamp: Instant, wasAutocomplete: Boolean): Int {
        return testSubject.addCompletion(
            Completion(
                taskId = taskId,
                completionTimestamp = completionTimestamp,
                dueDateWhenCompleted = today(),
                wasAutocomplete = wasAutocomplete,
            )
        )
    }
}