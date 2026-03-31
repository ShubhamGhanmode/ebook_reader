package com.shubhamghanmode.inkfold.feature.reader.preferences

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.preferences.Color as ReadiumColor
import org.readium.r2.navigator.preferences.Theme
import org.readium.r2.shared.ExperimentalReadiumApi

@OptIn(ExperimentalReadiumApi::class)
class ReaderPreferenceNormalizerTest {

    @Test
    fun normalizeAppliesInkFoldSepiaColors() {
        val normalized = ReaderPreferenceNormalizer.normalize(
            EpubPreferences(theme = Theme.SEPIA)
        )

        assertEquals(Theme.SEPIA, normalized.theme)
        assertEquals(0xFFFFECC3.toInt(), normalized.backgroundColor?.int)
        assertEquals(0xFF121212.toInt(), normalized.textColor?.int)
    }

    @Test
    fun normalizeClearsCustomColorsWhenThemeChangesAwayFromSepia() {
        val normalized = ReaderPreferenceNormalizer.normalize(
            EpubPreferences(
                theme = Theme.LIGHT,
                backgroundColor = ReadiumColor(0xFFFFECC3.toInt()),
                textColor = ReadiumColor(0xFF121212.toInt())
            )
        )

        assertEquals(Theme.LIGHT, normalized.theme)
        assertNull(normalized.backgroundColor)
        assertNull(normalized.textColor)
    }
}
