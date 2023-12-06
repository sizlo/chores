package com.timsummertonbrier.chores.domain.serde

import io.micronaut.core.type.Argument
import io.micronaut.serde.*
import jakarta.inject.Singleton
import kotlinx.datetime.LocalDate

@Singleton
class LocalDateSerde : Serde<LocalDate?> {
    override fun serialize(
        encoder: Encoder,
        context: Serializer.EncoderContext,
        type: Argument<out LocalDate?>,
        value: LocalDate?
    ) {
        value?.let { encoder.encodeString(it.toString()) } ?: encoder.encodeNull()
    }

    override fun deserialize(
        decoder: Decoder,
        context: Deserializer.DecoderContext,
        type: Argument<in LocalDate?>
    ): LocalDate? {
        val string = decoder.decodeString()
        if (string.isNullOrBlank()) {
            return null
        }
        return LocalDate.parse(string)
    }
}