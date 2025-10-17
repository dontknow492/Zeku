package org.ghost.zeku.database.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//data class MediaHistory()
@Serializable // <-- NEW: Required for Kotlinx.serialization
@Parcelize
data class Format(
    @SerialName("format_id")
    var formatId: String = "",

    @SerialName("ext")
    var container: String = "",

    @SerialName("vcodec")
    var vCodec: String = "",

    @SerialName("acodec")
    var aCodec: String = "",

    @SerialName("encoding")
    var encoding: String = "",

    @SerialName("filesize")
    var fileSize: Long = 0,

    @SerialName("format_note")
    var formatNote: String = "",

    @SerialName("fps")
    var fps: String? = "",

    @SerialName("asr")
    var asr: String? = "",

    @SerialName("url")
    var url: String? = "",

    @SerialName("language")
    val lang: String? = "",

    @SerialName("tbr")
    var tbr: String? = ""
) : Parcelable