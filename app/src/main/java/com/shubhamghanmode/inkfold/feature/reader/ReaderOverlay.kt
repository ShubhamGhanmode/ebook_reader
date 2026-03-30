package com.shubhamghanmode.inkfold.feature.reader

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shubhamghanmode.inkfold.R
import com.shubhamghanmode.inkfold.feature.reader.outline.ReaderOutlineSheet
import com.shubhamghanmode.inkfold.feature.reader.preferences.ReaderImageFilterOption
import com.shubhamghanmode.inkfold.feature.reader.preferences.ReaderPageMarginPreset
import com.shubhamghanmode.inkfold.feature.reader.preferences.ReaderScrollModeOption
import com.shubhamghanmode.inkfold.feature.reader.preferences.ReaderSettingsSheet
import com.shubhamghanmode.inkfold.feature.reader.preferences.ReaderThemeOption
import com.shubhamghanmode.inkfold.feature.reader.preferences.ReaderTypefaceOption
import com.shubhamghanmode.inkfold.feature.reader.tts.ReaderTtsControls
import com.shubhamghanmode.inkfold.feature.reader.tts.ReaderTtsSettingsSheet
import kotlin.math.roundToInt

@Composable
fun ReaderOverlay(
    uiState: ReaderUiState,
    onNavigateBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onCloseSettings: () -> Unit,
    onThemeSelected: (ReaderThemeOption) -> Unit,
    onScrollModeSelected: (ReaderScrollModeOption) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onIncreaseFontSize: () -> Unit,
    onDecreaseFontSize: () -> Unit,
    onTypefaceSelected: (ReaderTypefaceOption) -> Unit,
    onImageFilterSelected: (ReaderImageFilterOption) -> Unit,
    onPageMarginPresetSelected: (ReaderPageMarginPreset) -> Unit,
    onIncreasePageMargins: () -> Unit,
    onDecreasePageMargins: () -> Unit,
    onJumpToProgression: (Float) -> Unit,
    onSliderChangeFinished: () -> Unit,
    onResetAppearance: () -> Unit,
    onOpenOutline: () -> Unit,
    onCloseOutline: () -> Unit,
    onOutlineSectionSelected: (Int) -> Unit,
    onOutlineItemSelected: (String) -> Unit,
    onRequestReadAloud: () -> Unit,
    onToggleReadAloudPlayback: () -> Unit,
    onReadPreviousUtterance: () -> Unit,
    onReadNextUtterance: () -> Unit,
    onStopReadAloud: () -> Unit,
    onOpenReadAloudSettings: () -> Unit,
    onCloseReadAloudSettings: () -> Unit,
    onReadAloudSpeedChange: (Double) -> Unit,
    onReadAloudPitchChange: (Double) -> Unit,
    onReadAloudLanguageSelected: (String?) -> Unit,
    onReadAloudVoiceSelected: (String?) -> Unit,
    onDismissMissingVoiceDataPrompt: () -> Unit,
    onInstallMissingVoiceData: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(
        enabled = uiState.tts.isSettingsVisible || uiState.navigation.outline.isVisible || uiState.isSettingsVisible,
        onBack = {
            when {
                uiState.tts.isSettingsVisible -> onCloseReadAloudSettings()
                uiState.navigation.outline.isVisible -> onCloseOutline()
                uiState.isSettingsVisible -> onCloseSettings()
            }
        },
    )

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        PageFlipOverlay(
            state = uiState.pageFlipOverlayState,
            modifier = Modifier.fillMaxSize(),
        )

        if (uiState.tts.isActive) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        ),
            )
        }

        AnimatedVisibility(
            visible =
                uiState.isChromeVisible ||
                    uiState.isSettingsVisible ||
                    uiState.navigation.outline.isVisible ||
                    uiState.tts.isSettingsVisible,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            enter =
                fadeIn(animationSpec = tween(durationMillis = 180)) +
                    slideInVertically(
                        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                        initialOffsetY = { fullHeight -> -fullHeight / 2 },
                    ),
            exit =
                fadeOut(animationSpec = tween(durationMillis = 120)) +
                    slideOutVertically(
                        animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
                        targetOffsetY = { fullHeight -> -fullHeight / 2 },
                    ),
        ) {
            ReaderTopBar(
                title = uiState.title.ifBlank { stringResource(R.string.reader_toolbar_title) },
                onNavigateBack = onNavigateBack,
                onOpenSettings = onOpenSettings,
                onStartReadAloud = onRequestReadAloud,
                settingsEnabled = uiState.canOpenSettings,
                readAloudSupported = uiState.tts.isSupported,
                readAloudActive = uiState.tts.isActive,
            )
        }

        AnimatedVisibility(
            visible = uiState.isLoading,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(animationSpec = tween(durationMillis = 160)),
            exit = fadeOut(animationSpec = tween(durationMillis = 120)),
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                tonalElevation = 8.dp,
                shadowElevation = 18.dp,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(R.string.reader_loading),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        AnimatedVisibility(
            visible =
                (uiState.isChromeVisible || uiState.tts.isActive) &&
                    !uiState.isSettingsVisible &&
                    !uiState.navigation.outline.isVisible &&
                    !uiState.tts.isSettingsVisible &&
                    (uiState.navigation.isEnabled || uiState.tts.isActive),
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
            enter =
                fadeIn(animationSpec = tween(durationMillis = 180)) +
                    slideInVertically(
                        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                        initialOffsetY = { fullHeight -> fullHeight / 2 },
                    ),
            exit =
                fadeOut(animationSpec = tween(durationMillis = 120)) +
                    slideOutVertically(
                        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                        targetOffsetY = { fullHeight -> fullHeight / 2 },
                    ),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (uiState.tts.isActive) {
                    ReaderTtsControls(
                        uiState = uiState.tts,
                        onTogglePlayPause = onToggleReadAloudPlayback,
                        onPrevious = onReadPreviousUtterance,
                        onStop = onStopReadAloud,
                        onNext = onReadNextUtterance,
                        onOpenSettings = onOpenReadAloudSettings,
                    )
                }

                if (uiState.navigation.isEnabled) {
                    ReaderProgressSheet(
                        navigation = uiState.navigation,
                        onJumpToProgression = onJumpToProgression,
                        onOpenOutline = onOpenOutline,
                    )
                }
            }
        }

        ReaderSettingsSheet(
            isVisible = uiState.isSettingsVisible,
            appearance = uiState.appearance,
            onDismiss = onCloseSettings,
            onThemeSelected = onThemeSelected,
            onScrollModeSelected = onScrollModeSelected,
            onFontSizeChange = onFontSizeChange,
            onIncreaseFontSize = onIncreaseFontSize,
            onDecreaseFontSize = onDecreaseFontSize,
            onTypefaceSelected = onTypefaceSelected,
            onImageFilterSelected = onImageFilterSelected,
            onPageMarginPresetSelected = onPageMarginPresetSelected,
            onIncreasePageMargins = onIncreasePageMargins,
            onDecreasePageMargins = onDecreasePageMargins,
            onSliderChangeFinished = onSliderChangeFinished,
            onReset = onResetAppearance,
            modifier = Modifier.fillMaxSize(),
        )

        if (uiState.navigation.outline.isVisible) {
            ReaderOutlineSheet(
                uiState = uiState.navigation.outline,
                onDismiss = onCloseOutline,
                onSectionSelected = onOutlineSectionSelected,
                onItemSelected = onOutlineItemSelected,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (uiState.tts.isSettingsVisible) {
            ReaderTtsSettingsSheet(
                uiState = uiState.tts,
                onDismiss = onCloseReadAloudSettings,
                onSpeedChange = onReadAloudSpeedChange,
                onPitchChange = onReadAloudPitchChange,
                onLanguageSelected = onReadAloudLanguageSelected,
                onVoiceSelected = onReadAloudVoiceSelected,
                modifier = Modifier.fillMaxSize(),
            )
        }

        uiState.tts.missingVoiceDataLanguageLabel?.let { languageLabel ->
            AlertDialog(
                onDismissRequest = onDismissMissingVoiceDataPrompt,
                title = {
                    Text(text = stringResource(R.string.reader_tts_missing_voice_title))
                },
                text = {
                    Text(
                        text =
                            stringResource(
                                R.string.reader_tts_missing_voice_body,
                                languageLabel,
                            ),
                    )
                },
                confirmButton = {
                    FilledTonalButton(
                        onClick = {
                            onInstallMissingVoiceData()
                        },
                    ) {
                        Text(text = stringResource(R.string.reader_tts_install_voice))
                    }
                },
                dismissButton = {
                    FilledTonalButton(onClick = onDismissMissingVoiceDataPrompt) {
                        Text(text = stringResource(R.string.reader_tts_not_now))
                    }
                },
            )
        }
    }
}

@Composable
private fun ReaderTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onStartReadAloud: () -> Unit,
    settingsEnabled: Boolean,
    readAloudSupported: Boolean,
    readAloudActive: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 8.dp,
        shadowElevation = 18.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
//                    .heightIn(min = 64.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                contentAlignment = Alignment.Center,
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.reader_navigate_back),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = stringResource(R.string.reader_overlay_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (readAloudSupported) {
                    IconButton(onClick = onStartReadAloud) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = stringResource(R.string.reader_start_read_aloud),
                            tint =
                                if (readAloudActive) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                        )
                    }
                }

                IconButton(
                    onClick = onOpenSettings,
                    enabled = settingsEnabled,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = stringResource(R.string.reader_open_settings),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReaderProgressSheet(
    navigation: ReaderNavigationUiState,
    onJumpToProgression: (Float) -> Unit,
    onOpenOutline: () -> Unit,
) {
    var sliderValue by remember(navigation.totalPositions) {
        mutableFloatStateOf(navigation.currentProgression)
    }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(navigation.currentProgression, navigation.totalPositions, isDragging) {
        if (!isDragging) {
            sliderValue = navigation.currentProgression
        }
    }

    val previewPosition =
        if (navigation.totalPositions > 0) {
            (1 + (sliderValue * (navigation.totalPositions - 1)).roundToInt())
                .coerceIn(1, navigation.totalPositions)
        } else {
            0
        }
    val previewPercent = (sliderValue * 100f).roundToInt()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 8.dp,
        shadowElevation = 18.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                        .fillMaxWidth(0.16f)
                        .height(4.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.reader_progress_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text =
                            stringResource(
                                R.string.reader_progress_position,
                                previewPosition,
                                navigation.totalPositions,
                            ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.reader_progress_percent, previewPercent),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (navigation.outline.hasSections) {
                    FilledTonalButton(onClick = onOpenOutline) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.List,
                            contentDescription = null,
                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            text = stringResource(R.string.reader_outline_contents_button),
//                        )
                    }
                }
            }

            Slider(
                value = sliderValue,
                onValueChange = { newValue ->
                    isDragging = true
                    sliderValue = newValue
                },
                onValueChangeFinished = {
                    isDragging = false
                    onJumpToProgression(sliderValue)
                },
                valueRange = 0f..1f,
            )
        }
    }
}
