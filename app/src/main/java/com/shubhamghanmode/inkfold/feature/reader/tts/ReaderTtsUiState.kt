package com.shubhamghanmode.inkfold.feature.reader.tts

data class ReaderTtsUiState(
    val isSupported: Boolean = false,
    val isActive: Boolean = false,
    val isPlaying: Boolean = false,
    val hasHighlight: Boolean = false,
    val canOpenSettings: Boolean = false,
    val canSkipPrevious: Boolean = false,
    val canSkipNext: Boolean = false,
    val currentUtterance: String? = null,
    val currentLocationLabel: String? = null,
    val isSettingsVisible: Boolean = false,
    val speed: Double = 1.0,
    val speedLabel: String = "1.0x",
    val pitch: Double = 1.0,
    val pitchLabel: String = "1.0x",
    val selectedLanguageLabel: String = "",
    val availableLanguages: List<ReaderTtsLanguageOption> = emptyList(),
    val selectedVoiceLabel: String = "",
    val availableVoices: List<ReaderTtsVoiceOption> = emptyList(),
    val missingVoiceDataLanguageLabel: String? = null
)

data class ReaderTtsLanguageOption(
    val code: String?,
    val label: String
)

data class ReaderTtsVoiceOption(
    val id: String?,
    val label: String,
    val supportingLabel: String? = null
)
