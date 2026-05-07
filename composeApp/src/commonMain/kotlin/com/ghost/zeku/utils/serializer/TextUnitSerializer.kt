package com.ghost.zeku.utils.serializer

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object TextUnitSerializer : KSerializer<TextUnit> {

    // 2. A private data class that represents how it will look in JSON
    @Serializable
    @SerialName("TextUnit")
    private data class TextUnitSurrogate(
        val value: Float,
        val type: String
    )

    override val descriptor = TextUnitSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: TextUnit) {
        // Convert the Compose TextUnit into our easily serializable String/Float pair
        val typeStr = when (value.type) {
            TextUnitType.Sp -> "Sp"
            TextUnitType.Em -> "Em"
            else -> "Unspecified"
        }
        // 🌟 FIX: Check for NaN. If it is NaN, save it as 0f to prevent JSON crashes.
        val safeValue = if (value.value.isNaN()) 0f else value.value

        val surrogate = TextUnitSurrogate(safeValue, typeStr)
        encoder.encodeSerializableValue(TextUnitSurrogate.serializer(), surrogate)
    }

    override fun deserialize(decoder: Decoder): TextUnit {
        // Convert the JSON back into a Compose TextUnit
        val surrogate = decoder.decodeSerializableValue(TextUnitSurrogate.serializer())
        return when (surrogate.type) {
            "Sp" -> surrogate.value.sp
            "Em" -> surrogate.value.em
            else -> TextUnit.Unspecified
        }
    }
}