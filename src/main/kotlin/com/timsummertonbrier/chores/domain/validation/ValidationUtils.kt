package com.timsummertonbrier.chores.domain.validation

import jakarta.validation.ConstraintValidatorContext
import java.lang.NumberFormatException

fun ConstraintValidatorContext.addViolation(message: String, propertyName: String? = null) {
    val builder = this.buildConstraintViolationWithTemplate(message)
    propertyName?.let {
        builder.addPropertyNode(propertyName)
    }
    builder.addConstraintViolation()
}

fun String.isInt(): Boolean {
    try {
        this.toInt()
    } catch (e: NumberFormatException) {
        return false
    }
    return true
}