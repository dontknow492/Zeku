package com.ghost.zeku.domain.model

sealed interface MessageType {
    object Info : MessageType
    object Success : MessageType
    sealed interface Error : MessageType {
        object Network : Error
        object Database : Error
        object Unknown : Error
    }

    object Warning : MessageType

}