package com.shubhamghanmode.inkfold.feature.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PageFlipOverlay(
    state: PageFlipOverlayState,
    modifier: Modifier = Modifier
) {
    when (state) {
        PageFlipOverlayState.Disabled -> Unit
    }
}
