package com.timsummertonbrier.chores.domain.validation

import io.micronaut.core.annotation.Introspected
import jakarta.inject.Singleton
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlinx.datetime.LocalDate
import java.lang.Exception
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [DateValidator::class])
annotation class ValidDate (
    val message: String = "",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

@Singleton
@Introspected
class DateValidator : ConstraintValidator<ValidDate, String?> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        // Null will be handled by @NotNull if required
        if (value == null) {
            return true
        }

        if (wrongFormat(value)) {
            context.addViolation("Date is in incorrect format, please ues YYYY-MM-DD (e.g. 2023-12-25)")
            return false
        }

        if (invalid(value)) {
            context.addViolation("Date is invalid")
            return false
        }

        return true
    }

    private fun wrongFormat(value: String): Boolean {
        val isCorrect = """\d\d\d\d-\d\d-\d\d""".toRegex().matches(value)
        return !isCorrect
    }

    private fun invalid(value: String): Boolean {
        try {
            LocalDate.parse(value)
        } catch (e: Exception) {
            return true
        }
        return false
    }

}