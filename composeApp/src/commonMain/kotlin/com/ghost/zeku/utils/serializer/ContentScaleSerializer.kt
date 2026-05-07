package com.ghost.zeku.utils.serializer

import androidx.compose.ui.layout.ContentScale
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ContentScaleSerializer : KSerializer<ContentScale> {

    // We will serialize ContentScale simply as a String
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ContentScale", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ContentScale) {
        val scaleName = when (value) {
            ContentScale.Crop -> "Crop"
            ContentScale.Fit -> "Fit"
            ContentScale.FillBounds -> "FillBounds"
            ContentScale.FillHeight -> "FillHeight"
            ContentScale.FillWidth -> "FillWidth"
            ContentScale.Inside -> "Inside"
            ContentScale.None -> "None"
            else -> "Crop" // Safe fallback for custom scales
        }
        encoder.encodeString(scaleName)
    }

    override fun deserialize(decoder: Decoder): ContentScale {
        return when (decoder.decodeString()) {
            "Crop" -> ContentScale.Crop
            "Fit" -> ContentScale.Fit
            "FillBounds" -> ContentScale.FillBounds
            "FillHeight" -> ContentScale.FillHeight
            "FillWidth" -> ContentScale.FillWidth
            "Inside" -> ContentScale.Inside
            "None" -> ContentScale.None
            else -> ContentScale.Crop // Safe fallback for unknown strings
        }
    }
}



