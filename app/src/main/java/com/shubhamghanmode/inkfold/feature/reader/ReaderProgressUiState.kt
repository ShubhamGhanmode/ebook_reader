package com.shubhamghanmode.inkfold.feature.reader

data class ReaderProgressUiState(
    val isEnabled: Boolean = false,
    val currentProgression: Float = 0f,
    val currentPosition: Int = 0,
    val totalPositions: Int = 0
)
