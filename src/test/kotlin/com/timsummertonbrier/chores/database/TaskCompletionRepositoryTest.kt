package com.timsummertonbrier.chores.database

import com.timsummertonbrier.chores.domain.Completion
import com.timsummertonbrier.chores.domain.TaskRequest
import com.timsummertonbrier.chores.domain.TriggerType
import com.timsummertonbrier.chores.utils.now
import com.timsummertonbrier.chores.utils.today
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import kotlinx.datetime.Instant
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.days

@MicronautTest
class TaskCompletionRepositoryTest {

    @Inject
    private lateinit var taskRepository: TaskRepository

    @Inject
    private lateinit var testSubject: TaskCompletionRepository

    private var taskId: Int? = null

    @AfterEach
    fun afterEach() {
        taskId?.let { taskRepository.deleteTask(it) }
        taskId = null
    }

    @Test
    fun `selects latest non autocomplete completion`() {
        transaction {
            taskId = addTask()

            val base = now()

            // ignoredBecauseNotLatest
            addCompletion(completionTimestamp = base.plus(1.days), wasAutocomplete = false)
            // this one should be selected
            val expected = addCompletion(completionTimestamp = base.plus(2.days), wasAutocomplete = false)
            // ignoredBecauseNotLatest
            addCompletion(completionTimestamp = base.plus(1.days), wasAutocomplete = false)
            // ignoredBecauseAutocomplete
            addCompletion(completionTimestamp = base.plus(3.days), wasAutocomplete = true)

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

    private fun addCompletion(completionTimestamp: Instant, wasAutocomplete: Boolean): Int {
        return testSubject.addCompletion(
            Completion(
                taskId = taskId!!,
                completionTimestamp = completionTimestamp,
                dueDateWhenCompleted = today(),
                wasAutocomplete = wasAutocomplete,
            )
        )
    }
}