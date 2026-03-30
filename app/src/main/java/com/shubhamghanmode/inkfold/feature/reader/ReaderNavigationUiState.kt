package com.shubhamghanmode.inkfold.feature.reader

import com.shubhamghanmode.inkfold.feature.reader.outline.ReaderOutlineUiState

data class ReaderNavigationUiState(
    val isEnabled: Boolean = false,
    val currentProgression: Float = 0f,
    val currentPosition: Int = 0,
    val totalPositions: Int = 0,
    val outline: ReaderOutlineUiState = ReaderOutlineUiState()
)
