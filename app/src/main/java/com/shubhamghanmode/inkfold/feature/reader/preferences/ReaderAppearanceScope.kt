package com.shubhamghanmode.inkfold.feature.reader.preferences

sealed interface ReaderAppearanceScope {
    data object SharedDefaults : ReaderAppearanceScope

    data class Book(
        val bookId: Long
    ) : ReaderAppearanceScope
}
