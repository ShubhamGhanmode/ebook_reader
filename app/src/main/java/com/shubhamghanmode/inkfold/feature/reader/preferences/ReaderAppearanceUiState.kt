package com.shubhamghanmode.inkfold.feature.reader.preferences

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.shubhamghanmode.inkfold.R
import com.shubhamghanmode.inkfold.feature.reader.ReaderNavigatorConfiguration.LITERATA
import com.shubhamghanmode.inkfold.ui.theme.Ink
import com.shubhamghanmode.inkfold.ui.theme.NightAccent
import com.shubhamghanmode.inkfold.ui.theme.NightPaper
import com.shubhamghanmode.inkfold.ui.theme.Parchment
import com.shubhamghanmode.inkfold.ui.theme.ParchmentDeep
import org.readium.r2.navigator.preferences.FontFamily
import org.readium.r2.navigator.preferences.ImageFilter
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

@OptIn(ExperimentalReadiumApi::class)
enum class ReaderScrollModeOption(
    val readiumValue: Boolean,
    @param:StringRes val labelRes: Int
) {
    PAGED(
        readiumValue = false,
        labelRes = R.string.reader_settings_scroll_paged
    ),
    SCROLL(
        readiumValue = true,
        labelRes = R.string.reader_settings_scroll_scroll
    );

    companion object {
        fun from(value: Boolean): ReaderScrollModeOption =
            entries.firstOrNull { it.readiumValue == value } ?: PAGED
    }
}

@OptIn(ExperimentalReadiumApi::class)
enum class ReaderTypefaceOption(
    val readiumFontFamily: FontFamily?,
    @param:StringRes val labelRes: Int
) {
    ORIGINAL(
        readiumFontFamily = null,
        labelRes = R.string.reader_settings_typeface_original
    ),
    LITERATA(
        readiumFontFamily = FontFamily.LITERATA,
        labelRes = R.string.reader_settings_typeface_literata
    ),
    SERIF(
        readiumFontFamily = FontFamily.SERIF,
        labelRes = R.string.reader_settings_typeface_serif
    ),
    SANS_SERIF(
        readiumFontFamily = FontFamily.SANS_SERIF,
        labelRes = R.string.reader_settings_typeface_sans_serif
    ),
    IA_WRITER_DUOSPACE(
        readiumFontFamily = FontFamily.IA_WRITER_DUOSPACE,
        labelRes = R.string.reader_settings_typeface_ia_writer
    ),
    ACCESSIBLE_DFA(
        readiumFontFamily = FontFamily.ACCESSIBLE_DFA,
        labelRes = R.string.reader_settings_typeface_accessible_dfa
    ),
    OPEN_DYSLEXIC(
        readiumFontFamily = FontFamily.OPEN_DYSLEXIC,
        labelRes = R.string.reader_settings_typeface_open_dyslexic
    );

    companion object {
        fun from(fontFamily: FontFamily?): ReaderTypefaceOption =
            entries.firstOrNull { it.readiumFontFamily == fontFamily } ?: ORIGINAL
    }
}

enum class ReaderImageFilterOption(
    val readiumImageFilter: ImageFilter?,
    @param:StringRes val labelRes: Int
) {
    ORIGINAL(
        readiumImageFilter = null,
        labelRes = R.string.reader_settings_image_filter_original
    ),
    DARKEN(
        readiumImageFilter = ImageFilter.DARKEN,
        labelRes = R.string.reader_settings_image_filter_darken
    ),
    INVERT(
        readiumImageFilter = ImageFilter.INVERT,
        labelRes = R.string.reader_settings_image_filter_invert
    );

    companion object {
        fun from(imageFilter: ImageFilter?): ReaderImageFilterOption =
            entries.firstOrNull { it.readiumImageFilter == imageFilter } ?: ORIGINAL
    }
}

enum class ReaderPageMarginPreset(
    val value: Double,
    @param:StringRes val labelRes: Int
) {
    TIGHT(0.0, R.string.reader_settings_page_margins_tight),
    NARROW(0.5, R.string.reader_settings_page_margins_narrow),
    STANDARD(1.0, R.string.reader_settings_page_margins_standard),
    COMFORTABLE(1.5, R.string.reader_settings_page_margins_comfortable),
    WIDE(2.0, R.string.reader_settings_page_margins_wide)
}

data class ReaderAppearanceUiState(
    val supportsAppearanceControls: Boolean = false,
    val selectedTheme: ReaderThemeOption = ReaderThemeOption.LIGHT,
    val isScrollModeVisible: Boolean = false,
    val selectedScrollMode: ReaderScrollModeOption = ReaderScrollModeOption.PAGED,
    val textSize: Float = ReaderPreferenceNormalizer.TextSizeMax.toFloat(),
    val textSizeLabel: String = "",
    val textSizeRange: ClosedFloatingPointRange<Float> =
        ReaderPreferenceNormalizer.TextSizeMin.toFloat()..ReaderPreferenceNormalizer.TextSizeMax.toFloat(),
    val textSizeStep: Float = ReaderPreferenceNormalizer.TextSizeStep.toFloat(),
    val canDecreaseTextSize: Boolean = false,
    val canIncreaseTextSize: Boolean = false,
    val selectedTypeface: ReaderTypefaceOption = ReaderTypefaceOption.ORIGINAL,
    val selectedImageFilter: ReaderImageFilterOption = ReaderImageFilterOption.ORIGINAL,
    val isImageFilterEnabled: Boolean = false,
    @param:StringRes val imageFilterHelperMessageRes: Int? = null,
    val pageMarginPreset: ReaderPageMarginPreset = ReaderPageMarginPreset.STANDARD,
    val canDecreasePageMargins: Boolean = false,
    val canIncreasePageMargins: Boolean = false,
    val canReset: Boolean = false,
    @param:StringRes val helperMessageRes: Int? = null
)
