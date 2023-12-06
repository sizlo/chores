package com.timsummertonbrier.chores.domain.serde

import io.micronaut.core.type.Argument
import io.micronaut.serde.*
import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Singleton

@Target(AnnotationTarget.FIELD)
@Serdeable.Deserializable(using = EmptyStringToNullStringSerde::class)
annotation class EmptyStringToNull

abstract class EmptyStringToNullSerde<T> : Serde<T?> {
    override fun serialize(
        encoder: Encoder,
        context: Serializer.EncoderContext,
        type: Argument<out T?>,
        value: T?
    ) {
        value?.let { encode(encoder, it) } ?: encoder.encodeNull()
    }

    override fun deserialize(
        decoder: Decoder,
        context: Deserializer.DecoderContext,
        type: Argument<in T?>
    ): T? {
        val string = decoder.decodeString()
        if (string.isNullOrBlank()) {
            return null
        }
        return parse(string)
    }

    abstract fun parse(string: String): T
    abstract fun encode(encoder: Encoder, value: T)
}

@Singleton
class EmptyStringToNullStringSerde : EmptyStringToNullSerde<String>() {
    override fun parse(string: String): String {
        return string
    }

    override fun encode(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}
