package com.shubhamghanmode.inkfold.data.book

import org.readium.r2.shared.publication.Locator

object ProgressionNormalizer {
    fun fromLocator(locator: Locator): Float? =
        (locator.locations.totalProgression ?: locator.locations.progression)
            ?.coerceIn(0.0, 1.0)
            ?.toFloat()
}
