package com.shubhamghanmode.inkfold.feature.reader.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.preferences.ReadingProgression
import org.readium.r2.navigator.preferences.Theme
import org.readium.r2.shared.ExperimentalReadiumApi
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalReadiumApi::class)
class ReaderPreferencesManagerTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun sharedDefaultsFallBackToSystemThemeUntilOverridden() = runTest {
        val manager = createManager(
            defaultTheme = Theme.DARK,
            scope = backgroundScope
        )

        val sharedPreferences = manager.currentSharedPreferences()
        val effectivePreferences = manager.preferences.first()

        assertEquals(Theme.DARK, sharedPreferences.theme)
        assertEquals(Theme.DARK, effectivePreferences.theme)
        assertNull(sharedPreferences.fontSize)
    }

    @Test
    fun sharedAndPerBookPreferencesCombineAndResetIndependently() = runTest {
        val manager = createManager(
            defaultTheme = Theme.DARK,
            scope = backgroundScope
        )

        manager.setPreferences(
            scope = ReaderAppearanceScope.SharedDefaults,
            preferences = EpubPreferences(
                theme = Theme.SEPIA,
                fontSize = 1.4,
                pageMargins = 1.1
            )
        )

        manager.setPreferences(
            scope = ReaderAppearanceScope.Book(42L),
            preferences = EpubPreferences(readingProgression = ReadingProgression.RTL)
        )

        val effectivePreferences = manager.preferences.first()
        assertEquals(Theme.SEPIA, effectivePreferences.theme)
        assertEquals(1.4, effectivePreferences.fontSize ?: -1.0, 0.0001)
        assertEquals(1.1, effectivePreferences.pageMargins ?: -1.0, 0.0001)
        assertEquals(ReadingProgression.RTL, effectivePreferences.readingProgression)

        manager.reset(ReaderAppearanceScope.SharedDefaults)

        val sharedReset = manager.currentSharedPreferences()
        val effectiveAfterSharedReset = manager.preferences.first()
        assertEquals(Theme.DARK, sharedReset.theme)
        assertEquals(Theme.DARK, effectiveAfterSharedReset.theme)
        assertEquals(ReadingProgression.RTL, effectiveAfterSharedReset.readingProgression)

        manager.reset(ReaderAppearanceScope.Book(42L))

        val effectiveAfterBookReset = manager.preferences.first()
        assertEquals(Theme.DARK, effectiveAfterBookReset.theme)
        assertNull(effectiveAfterBookReset.readingProgression)
    }

    private fun createManager(
        defaultTheme: Theme,
        scope: CoroutineScope
    ): ReaderPreferencesManager {
        val dataStoreFile = File(
            temporaryFolder.root,
            "reader-preferences-${System.nanoTime()}.preferences_pb"
        )
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { dataStoreFile }
        )

        return ReaderPreferencesManager(
            bookId = 42L,
            dataStore = dataStore,
            defaultSharedPreferences = EpubPreferences(theme = defaultTheme)
        )
    }
}
