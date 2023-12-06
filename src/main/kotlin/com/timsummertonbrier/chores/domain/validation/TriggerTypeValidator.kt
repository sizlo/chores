package com.timsummertonbrier.chores.domain.validation

import com.timsummertonbrier.chores.domain.TriggerType
import io.micronaut.core.annotation.Introspected
import jakarta.inject.Singleton
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [TriggerTypeValidator::class])
annotation class ValidTriggerType (
    val message: String = "",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

@Singleton
@Introspected
class TriggerTypeValidator : ConstraintValidator<ValidTriggerType, String?> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        // Null will be handled by @NotNull if required
        if (value == null) {
            return true
        }

        if (!triggerTypeIsValid(value)) {
            context.addViolation("Must be one of ${TriggerType.entries.joinToString(", ") { it.name }}")
            return false
        }
        return true
    }
}

fun triggerTypeIsValid(triggerType: String?): Boolean {
    return TriggerType.entries.map { it.name }.contains(triggerType)
}