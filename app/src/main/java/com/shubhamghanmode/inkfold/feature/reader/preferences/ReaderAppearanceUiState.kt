package com.shubhamghanmode.inkfold.feature.reader.preferences

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.shubhamghanmode.inkfold.R
import com.shubhamghanmode.inkfold.ui.theme.Ink
import com.shubhamghanmode.inkfold.ui.theme.NightAccent
import com.shubhamghanmode.inkfold.ui.theme.NightPaper
import com.shubhamghanmode.inkfold.ui.theme.Parchment
import com.shubhamghanmode.inkfold.ui.theme.ParchmentDeep
import org.readium.r2.navigator.preferences.Theme
import org.readium.r2.shared.ExperimentalReadiumApi

@OptIn(ExperimentalReadiumApi::class)
enum class ReaderThemeOption(
    val readiumTheme: Theme,
    @param:StringRes val labelRes: Int,
    val previewBackground: Color,
    val previewForeground: Color,
    val accentColor: Color
) {
    LIGHT(
        readiumTheme = Theme.LIGHT,
        labelRes = R.string.reader_settings_theme_light,
        previewBackground = Color(0xFFFFFFFF),
        previewForeground = Color(0xFF121212),
        accentColor = ParchmentDeep
    ),
    SEPIA(
        readiumTheme = Theme.SEPIA,
        labelRes = R.string.reader_settings_theme_sepia,
        previewBackground = Color(0xFFFAF4E8),
        previewForeground = Ink,
        accentColor = Parchment
    ),
    DARK(
        readiumTheme = Theme.DARK,
        labelRes = R.string.reader_settings_theme_dark,
        previewBackground = NightPaper,
        previewForeground = Color(0xFFFEFEFE),
        accentColor = NightAccent
    );

    companion object {
        fun from(theme: Theme): ReaderThemeOption =
            entries.firstOrNull { it.readiumTheme == theme } ?: LIGHT
    }
}

data class ReaderAppearanceUiState(
    val supportsAppearanceControls: Boolean = false,
    val selectedTheme: ReaderThemeOption = ReaderThemeOption.LIGHT,
    val fontSize: Float = 1f,
    val fontSizeLabel: String = "",
    val fontSizeRange: ClosedFloatingPointRange<Float> = 0.1f..5f,
    val pageMargins: Float = 1f,
    val pageMarginsLabel: String = "",
    val pageMarginsRange: ClosedFloatingPointRange<Float> = 0f..4f,
    val canReset: Boolean = false,
    @param:StringRes val helperMessageRes: Int? = null
)
