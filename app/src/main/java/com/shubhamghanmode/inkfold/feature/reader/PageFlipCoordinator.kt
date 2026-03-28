package com.shubhamghanmode.inkfold.feature.reader

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface PageFlipOverlayState {
    data object Disabled : PageFlipOverlayState
}

/**
 * No-op coordinator reserved for the future interactive page-flip prototype.
 *
 * Keeping this seam in the reader host lets InkFold layer a dedicated overlay later without
 * reopening the reader architecture again.
 */
class PageFlipCoordinator(
    @Suppress("unused")
    private val pageSnapshotter: PageSnapshotter = PageSnapshotter()
) {
    private val _overlayState = MutableStateFlow<PageFlipOverlayState>(PageFlipOverlayState.Disabled)
    val overlayState: StateFlow<PageFlipOverlayState> = _overlayState.asStateFlow()
}
