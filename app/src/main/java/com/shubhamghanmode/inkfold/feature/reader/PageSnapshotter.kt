package com.shubhamghanmode.inkfold.feature.reader

/**
 * Placeholder seam for a future page-flip spike.
 *
 * The production reader does not capture page snapshots yet because the interactive flip path
 * still needs validation against Readium's rendering and gesture model.
 */
class PageSnapshotter {
    suspend fun captureVisiblePage(): PageSnapshot? = null
}

data class PageSnapshot(
    val width: Int,
    val height: Int
)
