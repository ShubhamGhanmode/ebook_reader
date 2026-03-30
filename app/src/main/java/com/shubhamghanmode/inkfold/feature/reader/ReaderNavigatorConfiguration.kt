package com.shubhamghanmode.inkfold.feature.reader

import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.navigator.epub.css.FontStyle
import org.readium.r2.navigator.preferences.FontFamily
import org.readium.r2.shared.ExperimentalReadiumApi

@OptIn(ExperimentalReadiumApi::class)
object ReaderNavigatorConfiguration {
    val FontFamily.Companion.LITERATA: FontFamily
        get() = FontFamily("Literata")

    fun create(): EpubNavigatorFragment.Configuration =
        EpubNavigatorFragment.Configuration {
            disablePageTurnsWhileScrolling = true
            servedAssets = servedAssets + "fonts/.*"

            addFontFamilyDeclaration(FontFamily.LITERATA) {
                addFontFace {
                    addSource("fonts/Literata-VariableFont_opsz,wght.ttf", preload = true)
                    setFontStyle(FontStyle.NORMAL)
                    setFontWeight(200..900)
                }
                addFontFace {
                    addSource("fonts/Literata-Italic-VariableFont_opsz,wght.ttf")
                    setFontStyle(FontStyle.ITALIC)
                    setFontWeight(200..900)
                }
            }
        }
}
