package com.timsummertonbrier.chores.domain.serde

import io.micronaut.core.type.Argument
import io.micronaut.serde.*
import jakarta.inject.Singleton
import kotlinx.datetime.Instant

@Singleton
class InstantSerde : Serde<Instant> {
    override fun serialize(
        encoder: Encoder,
        context: Serializer.EncoderContext,
        type: Argument<out Instant>,
        value: Instant
    ) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(
        decoder: Decoder,
        context: Deserializer.DecoderContext,
        type: Argument<in Instant>
    ): Instant {
        return Instant.parse(decoder.decodeString())
    }
}