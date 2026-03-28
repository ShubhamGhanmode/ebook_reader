package com.shubhamghanmode.inkfold.feature.reader

import com.shubhamghanmode.inkfold.feature.reader.preferences.ReaderPreferencesManager
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication

data class ReaderSession(
    val bookId: Long,
    val title: String,
    val publication: Publication,
    val navigatorFactory: EpubNavigatorFactory,
    val preferencesManager: ReaderPreferencesManager,
    val initialLocator: Locator?,
    val initialSharedPreferences: EpubPreferences,
    val initialBookPreferences: EpubPreferences,
    val initialPreferences: EpubPreferences
)
