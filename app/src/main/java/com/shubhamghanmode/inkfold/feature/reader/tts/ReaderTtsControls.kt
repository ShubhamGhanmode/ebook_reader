package com.shubhamghanmode.inkfold.feature.reader.tts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SettingsVoice
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shubhamghanmode.inkfold.R
import com.shubhamghanmode.inkfold.feature.reader.ReaderSheet

@Composable
fun ReaderTtsControls(
    uiState: ReaderTtsUiState,
    onTogglePlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onStop: () -> Unit,
    onNext: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        tonalElevation = 8.dp,
        shadowElevation = 18.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.reader_tts_controls_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            uiState.currentUtterance?.takeIf(String::isNotBlank)?.let { utterance ->
                Text(
                    text = utterance,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
//            uiState.currentLocationLabel?.takeIf(String::isNotBlank)?.let { locationLabel ->
//                Text(
//                    text = locationLabel,
//                    style = MaterialTheme.typography.labelMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(
                    onClick = onPrevious,
                    enabled = uiState.canSkipPrevious,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = stringResource(R.string.reader_tts_previous),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                FilledIconButton(onClick = onTogglePlayPause) {
                    Icon(
                        imageVector =
                            if (uiState.isPlaying) {
                                Icons.Rounded.Pause
                            } else {
                                Icons.Rounded.PlayArrow
                            },
                        contentDescription =
                            if (uiState.isPlaying) {
                                stringResource(R.string.reader_tts_pause)
                            } else {
                                stringResource(R.string.reader_tts_play)
                            },
                    )
                }
                FilledIconButton(
                    onClick = onStop,
                    colors =
                        IconButtonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                            disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.58f),
                            disabledContentColor = MaterialTheme.colorScheme.onError.copy(alpha = 0.58f),
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Stop,
                        contentDescription = stringResource(R.string.reader_tts_stop),
                    )
                }
                IconButton(
                    onClick = onNext,
                    enabled = uiState.canSkipNext,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = stringResource(R.string.reader_tts_next),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                IconButton(
                    onClick = onOpenSettings,
                    enabled = uiState.canOpenSettings,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = stringResource(R.string.reader_tts_open_settings),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
fun ReaderTtsSettingsSheet(
    uiState: ReaderTtsUiState,
    onDismiss: () -> Unit,
    onSpeedChange: (Double) -> Unit,
    onPitchChange: (Double) -> Unit,
    onLanguageSelected: (String?) -> Unit,
    onVoiceSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    ReaderSheet(
        title = stringResource(R.string.reader_tts_settings_title),
        subtitle = stringResource(R.string.reader_tts_settings_subtitle),
        onDismiss = onDismiss,
        modifier = modifier,
    ) {
        ReaderTtsSliderSection(
            title = stringResource(R.string.reader_tts_settings_speed),
            valueLabel = uiState.speedLabel,
            value = uiState.speed.toFloat(),
            onValueChange = { value -> onSpeedChange(value.toDouble()) },
        )
        ReaderTtsSliderSection(
            title = stringResource(R.string.reader_tts_settings_pitch),
            valueLabel = uiState.pitchLabel,
            value = uiState.pitch.toFloat(),
            onValueChange = { value -> onPitchChange(value.toDouble()) },
        )
        ReaderTtsChoiceSection(
            title = stringResource(R.string.reader_tts_settings_language),
            selectedLabel = uiState.selectedLanguageLabel,
            options = uiState.availableLanguages.map { option -> option.code to option.label },
            onOptionSelected = onLanguageSelected,
        )
        ReaderTtsChoiceSection(
            title = stringResource(R.string.reader_tts_settings_voice),
            selectedLabel = uiState.selectedVoiceLabel,
            options =
                uiState.availableVoices.map { option ->
                    option.id to
                        buildString {
                            append(option.label)
                            option.supportingLabel?.takeIf(String::isNotBlank)?.let { supportingLabel ->
                                append("  ")
                                append(supportingLabel)
                            }
                        }
                },
            onOptionSelected = onVoiceSelected,
        )
    }
}

@Composable
private fun ReaderTtsSliderSection(
    title: String,
    valueLabel: String,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(
            value = value.coerceIn(0.5f, 2.0f),
            onValueChange = { onValueChange((it * 10f).toInt() / 10f) },
            valueRange = 0.5f..2.0f,
        )
    }
}

@Composable
private fun ReaderTtsChoiceSection(
    title: String,
    selectedLabel: String,
    options: List<Pair<String?, String>>,
    onOptionSelected: (String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = selectedLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (optionId, label) ->
                OutlinedButton(
                    onClick = { onOptionSelected(optionId) },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
