package com.shubhamghanmode.inkfold.feature.reader.tts

import org.readium.navigator.media.tts.AndroidTtsNavigatorFactory
import org.readium.r2.shared.ExperimentalReadiumApi

@OptIn(ExperimentalReadiumApi::class)
data class ReaderTtsInitData(
    val navigatorFactory: AndroidTtsNavigatorFactory,
    val preferencesManager: ReaderTtsPreferencesManager
)
