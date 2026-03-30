package com.shubhamghanmode.inkfold.feature.reader.outline

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shubhamghanmode.inkfold.R
import com.shubhamghanmode.inkfold.feature.reader.ReaderSheet

@Composable
fun ReaderOutlineSheet(
    uiState: ReaderOutlineUiState,
    onDismiss: () -> Unit,
    onSectionSelected: (Int) -> Unit,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!uiState.hasSections) {
        return
    }

    ReaderSheet(
        title = stringResource(R.string.reader_outline_title),
        subtitle = stringResource(R.string.reader_outline_subtitle),
        onDismiss = onDismiss,
        modifier = modifier,
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            uiState.sections.forEachIndexed { index, section ->
                SegmentedButton(
                    shape =
                        SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = uiState.sections.size,
                        ),
                    onClick = { onSectionSelected(index) },
                    selected = index == uiState.selectedSectionIndex,
                    label = {
                        Text(
                            text = stringResource(section.id.labelRes),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(440.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = uiState.sections[uiState.selectedSectionIndex].items,
                key = ReaderOutlineItem::id,
            ) { item ->
                ReaderOutlineRow(
                    item = item,
                    onClick = { onItemSelected(item.id) },
                )
            }
        }
    }
}

@Composable
private fun ReaderOutlineRow(
    item: ReaderOutlineItem,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .padding(start = (item.depth * 12).dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(99.dp),
                        ).padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
//                Log.i("ITEM NUMBERING: ", "${item.depth}")
                Icon(
//                    text = "${item.depth + 1}",
                    imageVector = Icons.AutoMirrored.Rounded.List,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
