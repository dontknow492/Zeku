package com.ghost.zeku.utils.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@Serializable
object IntRangeSerializer : KSerializer<IntRange> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("IntRange", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: IntRange) {
        encoder.encodeInt(value.first)
        encoder.encodeInt(value.last)
    }

    override fun deserialize(decoder: Decoder): IntRange {
        val first = decoder.decodeInt()
        val last = decoder.decodeInt()
        return IntRange(first, last)
    }
}