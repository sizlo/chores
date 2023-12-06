package com.timsummertonbrier.chores.error

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class Errors (
    val classErrors: List<String>,
    val fieldErrors: Map<String, List<String>>
)