package com.ghost.zeku.utils.serializer

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object TextStyleSerializer : KSerializer<TextStyle> {

    // The Surrogate class that represents the JSON structure
    @Serializable
    @SerialName("TextStyle")
    private data class TextStyleSurrogate(
        val color: ULong = Color.Unspecified.value,

        @Serializable(with = TextUnitSerializer::class)
        val fontSize: TextUnit = TextUnit.Unspecified,

        val fontWeight: Int? = null,  // 400 = Normal, 700 = Bold
        val fontStyle: Int? = null,   // 0 = Normal, 1 = Italic

        @Serializable(with = TextUnitSerializer::class)
        val letterSpacing: TextUnit = TextUnit.Unspecified,

        val background: ULong = Color.Unspecified.value,
        val textAlign: String? = null, // Mapped to String for API safety

        @Serializable(with = TextUnitSerializer::class)
        val lineHeight: TextUnit = TextUnit.Unspecified
    )

    override val descriptor = TextStyleSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: TextStyle) {
        val surrogate = TextStyleSurrogate(
            color = value.color.value,
            fontSize = value.fontSize,
            fontWeight = value.fontWeight?.weight,
            fontStyle = value.fontStyle?.value,
            letterSpacing = value.letterSpacing,
            background = value.background.value,
            textAlign = value.textAlign?.let { encodeTextAlign(it) },
            lineHeight = value.lineHeight
        )
        encoder.encodeSerializableValue(TextStyleSurrogate.serializer(), surrogate)
    }

    override fun deserialize(decoder: Decoder): TextStyle {
        val surrogate = decoder.decodeSerializableValue(TextStyleSurrogate.serializer())

        return TextStyle(
            color = Color(surrogate.color),
            fontSize = surrogate.fontSize,
            fontWeight = surrogate.fontWeight?.let { FontWeight(it) },
            fontStyle = surrogate.fontStyle?.let { if (it == 1) FontStyle.Italic else FontStyle.Normal },
            letterSpacing = surrogate.letterSpacing,
            background = Color(surrogate.background),
            lineHeight = surrogate.lineHeight,
            textAlign = surrogate.textAlign?.let { decodeTextAlign(it) } ?: TextAlign.Unspecified,
        )
    }

    // --- Helpers to safely serialize Compose Enums/Value Classes ---

    private fun encodeTextAlign(align: TextAlign): String = when (align) {
        TextAlign.Left -> "Left"
        TextAlign.Right -> "Right"
        TextAlign.Center -> "Center"
        TextAlign.Justify -> "Justify"
        TextAlign.Start -> "Start"
        TextAlign.End -> "End"
        else -> "Unspecified"
    }

    private fun decodeTextAlign(align: String): TextAlign = when (align) {
        "Left" -> TextAlign.Left
        "Right" -> TextAlign.Right
        "Center" -> TextAlign.Center
        "Justify" -> TextAlign.Justify
        "Start" -> TextAlign.Start
        "End" -> TextAlign.End
        else -> TextAlign.Unspecified
    }
}