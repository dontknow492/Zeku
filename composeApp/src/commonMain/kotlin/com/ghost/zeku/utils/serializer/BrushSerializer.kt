package com.ghost.zeku.utils.serializer

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/* ========================================================================== */
/* BRUSH SERIALIZER */
/* ========================================================================== */

@Serializable
private data class SerializableBrush(
    val colors: List<Long>
)


object BrushSerializer : KSerializer<Brush> {

    override val descriptor: SerialDescriptor =
        SerializableBrush.serializer().descriptor

    override fun serialize(
        encoder: Encoder,
        value: Brush
    ) {

        val serializable = when (value) {

            is Brush -> {

                // ONLY supports linear gradients created
                // from color lists.

                val colorsField = value::class.java
                    .declaredFields
                    .firstOrNull {
                        it.name.contains("colors")
                    }

                colorsField?.isAccessible = true

                val colors = colorsField
                    ?.get(value) as? List<Color>
                    ?: emptyList()

                SerializableBrush(
                    colors = colors.map {
                        it.value.toLong()
                    }
                )
            }

            else -> {
                SerializableBrush(emptyList())
            }
        }

        encoder.encodeSerializableValue(
            SerializableBrush.serializer(),
            serializable
        )
    }

    override fun deserialize(decoder: Decoder): Brush {

        val value = decoder.decodeSerializableValue(
            SerializableBrush.serializer()
        )

        return Brush.verticalGradient(
            colors = value.colors.map {
                Color(it)
            }
        )
    }
}


