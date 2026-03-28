package com.shubhamghanmode.inkfold.feature.reader

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shubhamghanmode.inkfold.R
import com.shubhamghanmode.inkfold.feature.reader.preferences.ReaderSettingsSheet
import com.shubhamghanmode.inkfold.feature.reader.preferences.ReaderThemeOption

@Composable
fun ReaderOverlay(
    uiState: ReaderUiState,
    onNavigateBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onCloseSettings: () -> Unit,
    onThemeSelected: (ReaderThemeOption) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onPageMarginsChange: (Float) -> Unit,
    onSliderChangeFinished: () -> Unit,
    onResetAppearance: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(
        enabled = uiState.isSettingsVisible,
        onBack = onCloseSettings
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        PageFlipOverlay(
            state = uiState.pageFlipOverlayState,
            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(
            visible = uiState.isChromeVisible || uiState.isSettingsVisible,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            enter = fadeIn(animationSpec = tween(durationMillis = 180)) +
                slideInVertically(
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                    initialOffsetY = { fullHeight -> -fullHeight / 2 }
                ),
            exit = fadeOut(animationSpec = tween(durationMillis = 120)) +
                slideOutVertically(
                    animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
                    targetOffsetY = { fullHeight -> -fullHeight / 2 }
                )
        ) {
            ReaderTopBar(
                title = uiState.title.ifBlank { stringResource(R.string.reader_toolbar_title) },
                onNavigateBack = onNavigateBack,
                onOpenSettings = onOpenSettings,
                settingsEnabled = uiState.session != null && !uiState.isLoading
            )
        }

        AnimatedVisibility(
            visible = uiState.isLoading,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(animationSpec = tween(durationMillis = 160)),
            exit = fadeOut(animationSpec = tween(durationMillis = 120))
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                tonalElevation = 8.dp,
                shadowElevation = 18.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(R.string.reader_loading),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        ReaderSettingsSheet(
            isVisible = uiState.isSettingsVisible,
            appearance = uiState.appearance,
            onDismiss = onCloseSettings,
            onThemeSelected = onThemeSelected,
            onFontSizeChange = onFontSizeChange,
            onPageMarginsChange = onPageMarginsChange,
            onSliderChangeFinished = onSliderChangeFinished,
            onReset = onResetAppearance,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun ReaderTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    onOpenSettings: () -> Unit,
    settingsEnabled: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp)),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 8.dp,
        shadowElevation = 18.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.reader_navigate_back)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.reader_overlay_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onOpenSettings,
                enabled = settingsEnabled,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = stringResource(R.string.reader_open_settings)
                )
            }
        }
    }
}
