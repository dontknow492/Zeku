package com.ghost.zeku.utils.serializer

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import io.github.aakira.napier.Napier
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ColorSerializer : KSerializer<Color> {
    // We use STRING as the descriptor to allow for human-readable Hex formats
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Color) {
        if (value.isSpecified) {
            // Save as a clear Hex string (e.g., "ff6200ee")
            encoder.encodeString(value.value.toString(16))
        } else {
            // Fallback for Unspecified colors
            encoder.encodeString("00000000")
        }
    }

    override fun deserialize(decoder: Decoder): Color {
        val input = decoder.decodeString()
        return try {
            if (input.contains("x") || input.startsWith("#")) {
                // Handles "0xFF6200EE" or "#FF6200EE"
                val cleanHex = input.removePrefix("#").removePrefix("0x")
                Color(cleanHex.toULong(16))
            } else {
                // Handles raw ULong strings or old Long formats
                Color(input.toULong())
            }
        } catch (e: Exception) {
            Napier.w(e) { "Failed to parse color '$input'. Falling back to Transparent." }
            Color.Transparent // Lenient fallback to prevent app crashes
        }
    }
}


