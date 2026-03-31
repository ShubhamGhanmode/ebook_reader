package com.shubhamghanmode.inkfold.feature.reader.preferences

import kotlin.math.abs
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.preferences.FontFamily
import org.readium.r2.navigator.preferences.Theme
import org.readium.r2.shared.ExperimentalReadiumApi

@OptIn(ExperimentalReadiumApi::class)
object ReaderPreferenceNormalizer {
    const val TextSizeMin = 0.5
    const val TextSizeMax = 2.5
    const val TextSizeStep = 0.1

    val supportedFontFamilies: Set<FontFamily> = setOf(
        ReaderTypefaceOption.LITERATA.readiumFontFamily!!,
        FontFamily.SERIF,
        FontFamily.SANS_SERIF,
        FontFamily.IA_WRITER_DUOSPACE,
        FontFamily.ACCESSIBLE_DFA,
        FontFamily.OPEN_DYSLEXIC
    )

    fun normalize(preferences: EpubPreferences): EpubPreferences {
        val normalizedPreferences = preferences.copy(
            fontSize = preferences.fontSize?.let(::clampTextSize),
            pageMargins = preferences.pageMargins?.let { snapPageMargins(it).value },
            fontFamily = preferences.fontFamily?.takeIf { it in supportedFontFamilies }
        )
        return when (normalizedPreferences.theme) {
            Theme.LIGHT -> ReaderThemeOption.LIGHT.applyTo(normalizedPreferences)
            Theme.SEPIA -> ReaderThemeOption.SEPIA.applyTo(normalizedPreferences)
            Theme.DARK -> ReaderThemeOption.DARK.applyTo(normalizedPreferences)
            null -> normalizedPreferences
        }
    }

    fun clampTextSize(value: Double): Double =
        value.coerceIn(TextSizeMin, TextSizeMax)

    fun formatTextSize(value: Double): String =
        "${(clampTextSize(value) * 100).toInt()}%"

    fun snapPageMargins(value: Double): ReaderPageMarginPreset =
        ReaderPageMarginPreset.entries.minByOrNull { preset ->
            abs(preset.value - value)
        } ?: ReaderPageMarginPreset.STANDARD
}
