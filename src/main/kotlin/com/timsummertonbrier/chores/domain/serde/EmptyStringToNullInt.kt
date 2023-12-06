package com.timsummertonbrier.chores.domain.serde

import io.micronaut.core.type.Argument
import io.micronaut.serde.*
import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Singleton

@Target(AnnotationTarget.FIELD)
@Serdeable.Deserializable(using = EmptyStringToNullIntSerde::class)
annotation class EmptyStringToNullInt

@Singleton
class EmptyStringToNullIntSerde : Serde<Int?> {
    override fun serialize(
        encoder: Encoder,
        context: Serializer.EncoderContext,
        type: Argument<out Int?>,
        value: Int?
    ) {
        value?.let { encoder.encodeInt(it) } ?: encoder.encodeNull()
    }

    override fun deserialize(
        decoder: Decoder,
        context: Deserializer.DecoderContext,
        type: Argument<in Int?>
    ): Int? {
        val string = decoder.decodeString()
        if (string.isNullOrBlank()) {
            return null
        }
        return string.toInt()
    }
}