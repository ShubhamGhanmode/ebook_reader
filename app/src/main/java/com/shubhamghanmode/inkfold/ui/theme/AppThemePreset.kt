package com.shubhamghanmode.inkfold.ui.theme

import androidx.annotation.StringRes
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.shubhamghanmode.inkfold.R

enum class AppThemePreset(
    val storageKey: String,
    @param:StringRes val labelRes: Int,
    @param:StringRes val descriptionRes: Int,
    val seedColor: Color,
    private val palette: InkFoldPalette
) {
    IRON(
        storageKey = "iron",
        labelRes = R.string.app_theme_iron,
        descriptionRes = R.string.app_theme_iron_description,
        seedColor = Color(0xFF5A6168),
        palette = InkFoldPalettes.Iron
    ),
    CLASSIC(
        storageKey = "classic",
        labelRes = R.string.app_theme_classic,
        descriptionRes = R.string.app_theme_classic_description,
        seedColor = Color(0xFF9A6B2F),
        palette = InkFoldPalettes.Classic
    ),
    CYPRESS(
        storageKey = "cypress",
        labelRes = R.string.app_theme_cypress,
        descriptionRes = R.string.app_theme_cypress_description,
        seedColor = Color(0xFF5F7453),
        palette = InkFoldPalettes.Cypress
    ),
    HARBOR(
        storageKey = "harbor",
        labelRes = R.string.app_theme_harbor,
        descriptionRes = R.string.app_theme_harbor_description,
        seedColor = Color(0xFF46616D),
        palette = InkFoldPalettes.Harbor
    ),
    REDWOOD(
        storageKey = "redwood",
        labelRes = R.string.app_theme_redwood,
        descriptionRes = R.string.app_theme_redwood_description,
        seedColor = Color(0xFF7B2945),
        palette = InkFoldPalettes.Redwood
    ),
    MOSS(
        storageKey = "moss",
        labelRes = R.string.app_theme_moss,
        descriptionRes = R.string.app_theme_moss_description,
        seedColor = Color(0xFF667245),
        palette = InkFoldPalettes.Moss
    ),
    SLATE(
        storageKey = "slate",
        labelRes = R.string.app_theme_slate,
        descriptionRes = R.string.app_theme_slate_description,
        seedColor = Color(0xFF536675),
        palette = InkFoldPalettes.Slate
    ),
    AMBER(
        storageKey = "amber",
        labelRes = R.string.app_theme_amber,
        descriptionRes = R.string.app_theme_amber_description,
        seedColor = Color(0xFFFFBF00),
        palette = InkFoldPalettes.Amber
    );

    val previewSwatches: List<Color>
        get() = palette.previewSwatches

    fun colorScheme(darkTheme: Boolean): ColorScheme =
        if (darkTheme) {
            palette.dark
        } else {
            palette.light
        }

    companion object {
        fun fromStorageKey(storageKey: String?): AppThemePreset =
            entries.firstOrNull { it.storageKey == storageKey } ?: IRON
    }
}
