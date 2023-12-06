package com.timsummertonbrier.chores.domain.serde

import io.micronaut.serde.*
import jakarta.inject.Singleton
import kotlinx.datetime.LocalDate

@Singleton
class LocalDateSerde : EmptyStringToNullSerde<LocalDate>() {
    override fun parse(string: String): LocalDate {
        return LocalDate.parse(string)
    }

    override fun encode(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.toString())
    }

}