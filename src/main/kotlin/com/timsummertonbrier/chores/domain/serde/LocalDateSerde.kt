package com.timsummertonbrier.chores.domain.serde

import io.micronaut.core.type.Argument
import io.micronaut.serde.*
import jakarta.inject.Singleton
import kotlinx.datetime.LocalDate

@Singleton
class LocalDateSerde : Serde<LocalDate> {
    override fun serialize(
        encoder: Encoder,
        context: Serializer.EncoderContext,
        type: Argument<out LocalDate>,
        value: LocalDate
    ) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(
        decoder: Decoder,
        context: Deserializer.DecoderContext,
        type: Argument<in LocalDate>
    ): LocalDate {
        return LocalDate.parse(decoder.decodeString())
    }
}