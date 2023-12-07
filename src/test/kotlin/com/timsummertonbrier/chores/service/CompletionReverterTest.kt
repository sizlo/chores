package com.timsummertonbrier.chores.service

import com.timsummertonbrier.chores.database.TaskCompletionRepository
import com.timsummertonbrier.chores.database.TaskCompletions
import com.timsummertonbrier.chores.database.TaskRepository
import com.timsummertonbrier.chores.domain.*
import com.timsummertonbrier.chores.utils.now
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Primary
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.every
import io.mockk.spyk
import jakarta.inject.Inject
import kotlinx.datetime.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.RuntimeException

@MicronautTest
open class CompletionReverterTest {

    @Inject
    private lateinit var testSubject: CompletionReverter

    @Inject
    private lateinit var taskRepository: TaskRepository

    private val taskCompletionRepository = spyk<TaskCompletionRepository>()

    private var taskId: Int? = null

    @Bean
    @Primary
    fun taskCompletionRepository() = taskCompletionRepository

    @AfterEach
    fun afterEach() {
        taskId?.let { taskRepository.deleteTask(it) }
        taskId = null
    }

    @Test
    fun `updates due date and adds completion row in same transaction`() {
        taskId = addTask(dueDate = "2020-01-01")
        val completionId = addCompletion(dueDateWhenCompleted = "2019-01-01")

        every { taskCompletionRepository.addRevertedCompletion(any()) } throws RuntimeException()

        try {
            transaction { testSubject.revertLatestCompletion(taskId!!) }
        } catch (_: RuntimeException) {}

        assertCompletionStillExists(completionId)
        assertThat(taskRepository.findById(taskId!!).dueDate.toString()).isEqualTo("2020-01-01")

        taskRepository.deleteTask(taskId!!)
    }

    private fun addTask(dueDate: String): Int {
        return taskRepository.addTask(
            TaskRequest(
                name = "test task",
                dueDate = dueDate,
                autocomplete = false,
                triggerType = TriggerType.FIXED_DELAY.name,
                daysBetween = "10"
            )
        )
    }

    private fun addCompletion(dueDateWhenCompleted: String): Int {
        return transaction {
            taskCompletionRepository.addCompletion(
                Completion(
                    taskId = taskId!!,
                    completionTimestamp = now(),
                    dueDateWhenCompleted = LocalDate.parse(dueDateWhenCompleted),
                    wasAutocomplete = false
                )
            )
        }
    }

    private fun assertCompletionStillExists(completionId: Int) {
        val numCompletionsWithIds = transaction { TaskCompletions.select { TaskCompletions.id eq completionId }.count() }
        assertThat(numCompletionsWithIds).isGreaterThan(0)
    }
}
