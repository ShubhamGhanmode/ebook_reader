package com.shubhamghanmode.inkfold.feature.reader.outline

import androidx.annotation.StringRes
import com.shubhamghanmode.inkfold.R

enum class ReaderOutlineSectionId(
    @param:StringRes val labelRes: Int
) {
    CONTENTS(R.string.reader_outline_contents),
    PAGES(R.string.reader_outline_pages),
    LANDMARKS(R.string.reader_outline_landmarks)
}

data class ReaderOutlineSection(
    val id: ReaderOutlineSectionId,
    val items: List<ReaderOutlineItem>
)
