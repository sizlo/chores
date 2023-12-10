package com.timsummertonbrier.chores.domain.validation

import com.timsummertonbrier.chores.domain.TaskRequest
import com.timsummertonbrier.chores.domain.TriggerType
import com.timsummertonbrier.chores.utils.monthName
import io.micronaut.core.annotation.Introspected
import jakarta.inject.Singleton
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Constraint(validatedBy = [TriggerParametersValidator::class])
annotation class ValidTriggerParameters (
    val message: String = "",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

@Singleton
@Introspected
class TriggerParametersValidator
    : ConstraintValidator<ValidTriggerParameters, TaskRequest> {
    override fun isValid(value: TaskRequest, context: ConstraintValidatorContext): Boolean {
        // Can't validate trigger params if the trigger type is invalid
        if (!triggerTypeIsValid(value.triggerType)) {
            return true
        }

        return when (value.triggerTypeAsEnum()) {
            TriggerType.FIXED_DELAY -> isValidForFixedDelay(value, context)
            TriggerType.WEEKLY -> isValidForWeekly(value, context)
            TriggerType.MONTHLY -> isValidForMonthly(value, context)
            TriggerType.YEARLY -> isValidForYearly(value, context)
            TriggerType.ONE_OFF -> true
        }
    }

    private fun isValidForFixedDelay(value: TaskRequest, context: ConstraintValidatorContext): Boolean {
        if (value.daysBetween == null) {
            context.addViolation("Must be set for Fixed Delay triggers", propertyName = "daysBetween")
            return false
        }
        if (!value.daysBetween.isInt()) {
            context.addViolation("Must be an integer", propertyName = "daysBetween")
            return false
        }
        if (value.daysBetween.toInt() < 1) {
            context.addViolation("Must be positive", propertyName = "daysBetween")
            return false
        }
        return true
    }

    private fun isValidForWeekly(value: TaskRequest, context: ConstraintValidatorContext): Boolean {
        if (value.dayOfWeek == null) {
            context.addViolation("Must be set for Weekly triggers", propertyName = "dayOfWeek")
            return false
        }
        if (!value.dayOfWeek.isInt()) {
            context.addViolation("Must be an integer", propertyName = "dayOfWeek")
            return false
        }
        if (value.dayOfWeek.toInt() < 1 || value.dayOfWeek.toInt() > 7) {
            context.addViolation("Must be between 1 and 7 (inclusive)", propertyName = "dayOfWeek")
            return false
        }
        return true
    }

    private fun isValidForMonthly(value: TaskRequest, context: ConstraintValidatorContext): Boolean {
        if (value.dayOfMonth == null) {
            context.addViolation("Must be set for Monthly triggers", propertyName = "dayOfMonth")
            return false
        }
        if (!value.dayOfMonth.isInt()) {
            context.addViolation("Must be an integer", propertyName = "dayOfMonth")
            return false
        }
        if (value.dayOfMonth.toInt() < 1 || value.dayOfMonth.toInt() > 31) {
            context.addViolation("Must be between 1 and 31 (inclusive)", propertyName = "dayOfMonth")
            return false
        }
        return true
    }


    private fun isValidForYearly(value: TaskRequest, context: ConstraintValidatorContext): Boolean {
        var valid = true

        if (value.monthOfYear == null) {
            context.addViolation("Must be set for Yearly triggers", propertyName = "monthOfYear")
            valid = false
        }

        if (value.dayOfMonth == null) {
            context.addViolation("Must be set for Yearly triggers", propertyName = "dayOfMonth")
            valid = false
        }

        if (value.monthOfYear != null) {

            if (!value.monthOfYear.isInt()) {
                context.addViolation("Must be an integer", propertyName = "monthOfYear")
                valid = false
            }

            if (value.monthOfYear.isInt() && (value.monthOfYear.toInt() < 1 || value.monthOfYear.toInt() > 12)) {
                context.addViolation("Must be between 1 and 12 (inclusive)", propertyName = "monthOfYear")
                valid = false
            }

            if (value.dayOfMonth != null) {
                if (!value.dayOfMonth.isInt()) {
                    context.addViolation("Must be an integer", propertyName = "dayOfMonth")
                    valid = false
                }

                val maxDayOfMonth = getMaxDayOfMonth(value.monthOfYear.toInt())
                if (value.dayOfMonth.isInt() && (value.dayOfMonth.toInt() < 1 || value.dayOfMonth.toInt() > maxDayOfMonth)) {
                    context.addViolation(
                        "Must be between 1 and $maxDayOfMonth (inclusive) for ${value.monthOfYear.toInt().monthName()}",
                        propertyName = "dayOfMonth"
                    )
                    valid = false
                }
            }
        }

        return valid
    }

    private fun getMaxDayOfMonth(month: Int?): Int {
        return when (month) {
            1 -> 31
            2 -> 29
            3 -> 31
            4 -> 30
            5 -> 31
            6 -> 30
            7 -> 31
            8 -> 31
            9 -> 30
            10 -> 31
            11 -> 30
            12 -> 31
            else -> 31
        }
    }
}