package com.shubhamghanmode.inkfold.feature.home

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shubhamghanmode.inkfold.R
import com.shubhamghanmode.inkfold.ui.theme.AppThemePreset

@Composable
fun AppSettingsSheet(
    isVisible: Boolean,
    selectedThemePreset: AppThemePreset,
    onDismiss: () -> Unit,
    onThemePresetSelected: (AppThemePreset) -> Unit,
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
        Box(modifier = Modifier.fillMaxSize()) {
            val scrimInteractionSource = remember { MutableInteractionSource() }

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.2f))
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
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        AppSettingsHeader(onDismiss = onDismiss)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = stringResource(R.string.app_settings_section_theme),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )

                            LazyColumn(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .weight(1f, fill = false),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(AppThemePreset.entries) { preset ->
                                    AppThemePresetCard(
                                        preset = preset,
                                        selected = preset == selectedThemePreset,
                                        onClick = { onThemePresetSelected(preset) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppSettingsHeader(onDismiss: () -> Unit) {
    Column {
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
                content = {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.app_settings_close),
                    )
                },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.app_settings_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.app_settings_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AppThemePresetCard(
    preset: AppThemePreset,
    selected: Boolean,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.elevatedCardColors(
                containerColor =
                    if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
            ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (selected) 6.dp else 1.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                preset.previewSwatches.forEach { swatch ->
                    Box(
                        modifier =
                            Modifier
                                .shadow(elevation = 3.dp, shape = CircleShape)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(swatch),
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(preset.labelRes),
                    style = MaterialTheme.typography.titleMedium,
                    color =
                        if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                )
                Text(
                    text = stringResource(preset.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            }

            if (selected) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        }
    }
}
