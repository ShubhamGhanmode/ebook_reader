package com.shubhamghanmode.inkfold.feature.reader.tts

sealed interface ReaderTtsError {
    data object UnsupportedPublication : ReaderTtsError
    data object InitializationFailed : ReaderTtsError
    data class EngineFailure(val detail: String? = null) : ReaderTtsError
    data class ContentFailure(val detail: String? = null) : ReaderTtsError
}
