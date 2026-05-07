package com.ghost.zeku.utils.serializer

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object PaddingValuesSerializer : KSerializer<PaddingValues> {

    // A simple surrogate to store the 4 sides as plain Floats
    @Serializable
    @SerialName("PaddingValues")
    private data class PaddingValuesSurrogate(
        val start: Float,
        val top: Float,
        val end: Float,
        val bottom: Float
    )

    override val descriptor = PaddingValuesSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: PaddingValues) {
        // We use LTR (Left-To-Right) to safely extract the "Start" and "End" values.
        // Start maps to Left, and End maps to Right in LTR mode.
        val surrogate = PaddingValuesSurrogate(
            start = value.calculateLeftPadding(LayoutDirection.Ltr).value,
            top = value.calculateTopPadding().value,
            end = value.calculateRightPadding(LayoutDirection.Ltr).value,
            bottom = value.calculateBottomPadding().value
        )
        encoder.encodeSerializableValue(PaddingValuesSurrogate.serializer(), surrogate)
    }

    override fun deserialize(decoder: Decoder): PaddingValues {
        val surrogate = decoder.decodeSerializableValue(PaddingValuesSurrogate.serializer())

        // Reconstruct as standard RTL-aware PaddingValues
        return PaddingValues(
            start = surrogate.start.dp,
            top = surrogate.top.dp,
            end = surrogate.end.dp,
            bottom = surrogate.bottom.dp
        )
    }
}