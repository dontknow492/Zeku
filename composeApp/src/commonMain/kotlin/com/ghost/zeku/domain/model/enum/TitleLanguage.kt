package com.ghost.zeku.domain.model.enum

import com.ghost.zeku.domain.model.common.MediaTitle

enum class TitleLanguage { ROMAJI, ENGLISH, NATIVE }


fun MediaTitle.getPreferred(language: TitleLanguage): String {
    return when (language) {
        TitleLanguage.ROMAJI -> romaji ?: english ?: native
        TitleLanguage.ENGLISH -> english ?: romaji ?: native
        TitleLanguage.NATIVE -> native ?: romaji ?: english
    } ?: "Unknown Title"
}