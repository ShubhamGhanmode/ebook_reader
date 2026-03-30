package com.shubhamghanmode.inkfold.feature.reader.preferences

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shubhamghanmode.inkfold.R

@Composable
fun ReaderSettingsSheet(
    isVisible: Boolean,
    appearance: ReaderAppearanceUiState,
    onDismiss: () -> Unit,
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
    onSliderChangeFinished: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(
        enabled = isVisible,
        onBack = onDismiss,
    )

    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(durationMillis = 180)),
        exit = fadeOut(animationSpec = tween(durationMillis = 150)),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            val scrimInteractionSource = remember { MutableInteractionSource() }

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.22f))
                        .clickable(
                            interactionSource = scrimInteractionSource,
                            indication = null,
                            onClick = onDismiss,
                        ),
            )

            AnimatedVisibility(
                visible = true,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter =
                    slideInVertically(
                        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                        initialOffsetY = { fullHeight -> fullHeight / 2 },
                    ) + fadeIn(animationSpec = tween(durationMillis = 220)),
                exit =
                    slideOutVertically(
                        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                        targetOffsetY = { fullHeight -> fullHeight / 2 },
                    ) + fadeOut(animationSpec = tween(durationMillis = 140)),
            ) {
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .navigationBarsPadding(),
                    shape =
                        RoundedCornerShape(
                            topStart = 30.dp,
                            topEnd = 30.dp,
                            bottomStart = 28.dp,
                            bottomEnd = 28.dp,
                        ),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                    tonalElevation = 10.dp,
                    shadowElevation = 20.dp,
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        ReaderSettingsHeader(onDismiss = onDismiss)

                        if (!appearance.supportsAppearanceControls) {
                            appearance.helperMessageRes?.let { helperMessageRes ->
                                Surface(
                                    shape = RoundedCornerShape(22.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.82f),
                                ) {
                                    Text(
                                        text = stringResource(helperMessageRes),
                                        modifier = Modifier.padding(18.dp),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        } else {
                            ReaderSettingsSection(title = stringResource(R.string.reader_settings_section_theme)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    ReaderThemeOption.entries.forEach { themeOption ->
                                        ReaderThemePreview(
                                            themeOption = themeOption,
                                            selected = themeOption == appearance.selectedTheme,
                                            onClick = { onThemeSelected(themeOption) },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }

                            if (appearance.isScrollModeVisible) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                                ReaderSettingsSection(title = stringResource(R.string.reader_settings_section_layout)) {
                                    ReaderChoiceRow(
                                        options = ReaderScrollModeOption.entries,
                                        selectedOption = appearance.selectedScrollMode,
                                        optionLabel = { option -> stringResource(option.labelRes) },
                                        onOptionSelected = onScrollModeSelected,
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                            ReaderSettingsSection(
                                title = stringResource(R.string.reader_settings_section_text_size),
                                value = appearance.textSizeLabel,
                            ) {
                                ReaderFontPreview(
                                    fontSize = appearance.textSize,
                                    canDecrease = appearance.canDecreaseTextSize,
                                    canIncrease = appearance.canIncreaseTextSize,
                                    onDecrease = onDecreaseFontSize,
                                    onIncrease = onIncreaseFontSize,
                                )
                                Slider(
                                    value = appearance.textSize,
                                    onValueChange = onFontSizeChange,
                                    onValueChangeFinished = onSliderChangeFinished,
                                    valueRange = appearance.textSizeRange,
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                            ReaderSettingsSection(
                                title = stringResource(R.string.reader_settings_section_typeface),
                            ) {
                                ReaderTypefaceGrid(
                                    selectedTypeface = appearance.selectedTypeface,
                                    onTypefaceSelected = onTypefaceSelected,
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                            ReaderSettingsSection(
                                title = stringResource(R.string.reader_settings_section_image_filter),
                            ) {
                                ReaderChoiceRow(
                                    options = ReaderImageFilterOption.entries,
                                    selectedOption = appearance.selectedImageFilter,
                                    optionLabel = { option -> stringResource(option.labelRes) },
                                    enabled = appearance.isImageFilterEnabled,
                                    onOptionSelected = onImageFilterSelected,
                                )
                                appearance.imageFilterHelperMessageRes?.let { helperMessageRes ->
                                    Text(
                                        text = stringResource(helperMessageRes),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                            ReaderSettingsSection(
                                title = stringResource(R.string.reader_settings_section_page_margins),
                                value = stringResource(appearance.pageMarginPreset.labelRes),
                            ) {
                                ReaderPageMarginPreview(pageMargins = appearance.pageMarginPreset.value.toFloat())
                                ReaderChoiceRow(
                                    options = ReaderPageMarginPreset.entries,
                                    selectedOption = appearance.pageMarginPreset,
                                    optionLabel = { option -> stringResource(option.labelRes) },
                                    onOptionSelected = onPageMarginPresetSelected,
//                                    leadingActionLabel = stringResource(R.string.reader_settings_decrease),
//                                    trailingActionLabel = stringResource(R.string.reader_settings_increase),
                                    leadingActionEnabled = appearance.canDecreasePageMargins,
                                    trailingActionEnabled = appearance.canIncreasePageMargins,
                                    onLeadingAction = onDecreasePageMargins,
                                    onTrailingAction = onIncreasePageMargins,
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onReset,
                            enabled = appearance.canReset,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = PaddingValues(vertical = 14.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.reader_settings_reset),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderSettingsHeader(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(modifier = Modifier.padding(top = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            IconButton(
                onClick = onDismiss,
                colors =
                    IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.reader_settings_close),
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.reader_settings_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.reader_settings_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ReaderSettingsSection(
    title: String,
    value: String? = null,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            value?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        content()
    }
}

@Composable
private fun <T> ReaderChoiceRow(
    options: List<T>,
    selectedOption: T,
    optionLabel: @Composable (T) -> String,
    enabled: Boolean = true,
    leadingActionLabel: String? = null,
    trailingActionLabel: String? = null,
    leadingActionEnabled: Boolean = false,
    trailingActionEnabled: Boolean = false,
    onLeadingAction: (() -> Unit)? = null,
    onTrailingAction: (() -> Unit)? = null,
    onOptionSelected: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (leadingActionLabel != null && trailingActionLabel != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ReaderMiniAction(
                    label = leadingActionLabel,
                    enabled = enabled && leadingActionEnabled,
                    onClick = { onLeadingAction?.invoke() },
                    modifier = Modifier.weight(1f),
                )
                ReaderMiniAction(
                    label = trailingActionLabel,
                    enabled = enabled && trailingActionEnabled,
                    onClick = { onTrailingAction?.invoke() },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        options.chunked(2).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowOptions.forEach { option ->
                    val selected = option == selectedOption
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        color =
                            if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
                            },
                        tonalElevation = if (selected) 3.dp else 0.dp,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = enabled) { onOptionSelected(option) }
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = optionLabel(option),
                                style = MaterialTheme.typography.labelLarge,
                                color =
                                    if (selected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else if (enabled) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
                                    },
                            )
                        }
                    }
                }
                if (rowOptions.size == 1) {
                    SpacerSurface(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ReaderMiniAction(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
    ) {
        Text(text = label)
    }
}

@Composable
private fun SpacerSurface(modifier: Modifier = Modifier) {
    Box(modifier = modifier)
}

@Composable
private fun ReaderFontPreview(
    fontSize: Float,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    val normalizedProgress =
        (
            (fontSize - ReaderPreferenceNormalizer.TextSizeMin.toFloat()) /
                (ReaderPreferenceNormalizer.TextSizeMax.toFloat() - ReaderPreferenceNormalizer.TextSizeMin.toFloat())
        ).coerceIn(0f, 1f)
    val previewFontSize = (24f + (16f * normalizedProgress)).sp

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReaderLetterAction(
                label = stringResource(R.string.reader_settings_preview_small),
                enabled = canDecrease,
                onClick = onDecrease,
            )
            Text(
                text = stringResource(R.string.reader_settings_preview_letters),
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        fontSize = previewFontSize,
                    ),
            )
            ReaderLetterAction(
                label = stringResource(R.string.reader_settings_preview_large),
                enabled = canIncrease,
                onClick = onIncrease,
            )
        }
    }
}

@Composable
private fun ReaderLetterAction(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color =
            if (enabled) {
                MaterialTheme.colorScheme.background
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
            },
    ) {
        Box(
            modifier =
                Modifier
                    .clickable(enabled = enabled, onClick = onClick)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color =
                    if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
                    },
            )
        }
    }
}

@Composable
private fun ReaderTypefaceGrid(
    selectedTypeface: ReaderTypefaceOption,
    onTypefaceSelected: (ReaderTypefaceOption) -> Unit,
) {
    ReaderChoiceRow(
        options = ReaderTypefaceOption.entries,
        selectedOption = selectedTypeface,
        optionLabel = { option -> stringResource(option.labelRes) },
        onOptionSelected = onTypefaceSelected,
    )
}

@Composable
private fun ReaderPageMarginPreview(pageMargins: Float) {
    val normalizedProgress = (pageMargins / 2f).coerceIn(0f, 1f)
    val horizontalInset = 10.dp + (26.dp * normalizedProgress)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(116.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.96f))
                        .padding(horizontal = horizontalInset, vertical = 18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(3) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)),
                        )
                    }
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth(0.72f)
                                .height(10.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)),
                    )
                }
            }
        }
    }
}
