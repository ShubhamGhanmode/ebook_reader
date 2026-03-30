package com.shubhamghanmode.inkfold.feature.reader.outline

data class ReaderOutlineUiState(
    val isVisible: Boolean = false,
    val sections: List<ReaderOutlineSection> = emptyList(),
    val selectedSectionIndex: Int = 0
) {
    val hasSections: Boolean
        get() = sections.isNotEmpty()
}
