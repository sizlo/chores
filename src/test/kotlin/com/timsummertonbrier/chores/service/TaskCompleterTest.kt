package com.timsummertonbrier.chores.service

import com.timsummertonbrier.chores.database.TaskCompletionRepository
import com.timsummertonbrier.chores.database.TaskRepository
import com.timsummertonbrier.chores.domain.TaskRequest
import com.timsummertonbrier.chores.domain.TriggerType
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Primary
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.RuntimeException

@MicronautTest
open class TaskCompleterTest {

    @Inject
    private lateinit var testSubject: TaskCompleter

    @Inject
    private lateinit var taskRepository: TaskRepository

    private val taskCompletionRepository = mockk<TaskCompletionRepository>(relaxed = true)

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

        every { taskCompletionRepository.addCompletion(any()) } throws RuntimeException()

        try {
            testSubject.complete(taskId!!)
        } catch (_: RuntimeException) {}

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
}
