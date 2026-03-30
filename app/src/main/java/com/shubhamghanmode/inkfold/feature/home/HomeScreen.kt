package com.shubhamghanmode.inkfold.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.ImportContacts
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.shubhamghanmode.inkfold.ui.theme.AppThemePreset
import java.io.File
import kotlin.math.roundToInt
import androidx.compose.foundation.lazy.items as lazyRowItems

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    selectedThemePreset: AppThemePreset = AppThemePreset.CLASSIC,
    onImportClick: () -> Unit,
    onThemePresetSelected: (AppThemePreset) -> Unit = {},
    onOpenBook: (Long) -> Unit,
    onDeleteBook: (Long) -> Unit,
    onTransientMessageShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingDeletion by remember { mutableStateOf<HomeBook?>(null) }
    var isAppSettingsVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.transientMessage) {
        val message = uiState.transientMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        onTransientMessageShown()
    }

    Box(modifier = modifier.fillMaxSize()) {
        EditorialBackground()

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        ) { innerPadding ->
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 152.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        start = 20.dp,
                        top = innerPadding.calculateTopPadding() + 12.dp,
                        end = 20.dp,
                        bottom = innerPadding.calculateBottomPadding() + 28.dp,
                    ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ShelfTopBar(
                        onImportClick = onImportClick,
                        onOpenSettings = { isAppSettingsVisible = true },
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    when {
                        uiState.isEmpty -> {
                            EmptyShelfCard()
                        }

                        uiState.continueReading != null -> {
                            ContinueReadingCard(
                                book = uiState.continueReading,
                                onOpenBook = onOpenBook,
                            )
                        }

                        else -> {
                            NoCurrentReadCard(bookCount = uiState.allBooks.size)
                        }
                    }
                }

                if (uiState.recentImports.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SectionHeader(
                            eyebrow = "Fresh on the shelf",
                            title = "Recent Imports",
                        )
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        RecentImportsRow(
                            books = uiState.recentImports,
                            onOpenBook = onOpenBook,
                            onDeleteBook = { pendingDeletion = it },
                        )
                    }
                }

                if (uiState.allBooks.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SectionHeader(
                            eyebrow = "Your library",
                            title = "All Books",
                        )
                    }

                    items(
                        items = uiState.allBooks,
                        key = { book -> book.id },
                    ) { book ->
                        BookCard(
                            book = book,
                            onOpenBook = onOpenBook,
                            onDeleteBook = { pendingDeletion = it },
                        )
                    }
                }
            }
        }
    }

    pendingDeletion?.let { book ->
        DeleteBookDialog(
            book = book,
            onDismiss = { pendingDeletion = null },
            onConfirm = {
                onDeleteBook(book.id)
                pendingDeletion = null
            },
        )
    }

    AppSettingsSheet(
        isVisible = isAppSettingsVisible,
        selectedThemePreset = selectedThemePreset,
        onDismiss = { isAppSettingsVisible = false },
        onThemePresetSelected = onThemePresetSelected,
    )
}

@Composable
private fun EditorialBackground() {
    val tertiaryHighlight = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.18f)
    val primaryHighlight = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
    val secondaryHighlight = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.16f)

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.background,
                                ),
                        ),
                ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = tertiaryHighlight,
                radius = size.minDimension * 0.34f,
                center = Offset(x = size.width * 0.16f, y = size.height * 0.14f),
            )
            drawCircle(
                color = primaryHighlight,
                radius = size.minDimension * 0.28f,
                center = Offset(x = size.width * 0.92f, y = size.height * 0.26f),
            )
            drawCircle(
                color = secondaryHighlight,
                radius = size.minDimension * 0.22f,
                center = Offset(x = size.width * 0.74f, y = size.height * 0.82f),
            )
        }
    }
}

@Composable
private fun ShelfTopBar(
    onImportClick: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "InkFold",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "A quiet shelf for the EPUBs you keep.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 8.dp,
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                IconButton(
                    onClick = onOpenSettings,
                    colors =
                        IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Palette,
                        contentDescription = "Open app settings",
                    )
                }
            }

            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 8.dp,
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                IconButton(
                    onClick = onImportClick,
                    modifier = Modifier.testTag("import-button"),
                    colors =
                        IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Import EPUB",
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyShelfCard() {
    ElevatedCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag("empty-library-card"),
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
            ),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.secondary,
                shadowElevation = 3.dp,
            ) {
                Icon(
                    imageVector = Icons.Rounded.ImportContacts,
                    contentDescription = null,
                    modifier = Modifier.padding(14.dp),
                    tint = MaterialTheme.colorScheme.onSecondary,
                )
            }

            Text(
                text = "Start with a personal shelf, not a storefront.",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
                text = "Import an EPUB from your device and InkFold will keep a clean local copy. EPUB files can also be imported by selecting 'InkFold' app in the 'Open with' menu when trying to open an EPUB file.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun NoCurrentReadCard(bookCount: Int) {
    ElevatedCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag("no-current-read-card"),
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            ),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.padding(14.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Text(
                text = "Your shelf is ready.",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "Choose from $bookCount local ${if (bookCount == 1) "book" else "books"} below and InkFold will remember where you leave off.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ContinueReadingCard(
    book: HomeBook,
    onOpenBook: (Long) -> Unit,
) {
    ElevatedCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag("continue-reading-card"),
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isCompact = maxWidth < 460.dp

            if (isCompact) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    BookCover(
                        coverPath = book.coverPath,
                        modifier =
                            Modifier
                                .width(140.dp)
                                .aspectRatio(2f / 3f),
                    )
                    ContinueReadingDetails(
                        book = book,
                        onOpenBook = onOpenBook,
                    )
                }
            } else {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BookCover(
                        coverPath = book.coverPath,
                        modifier =
                            Modifier
                                .width(112.dp)
                                .aspectRatio(2f / 3f),
                    )

                    ContinueReadingDetails(
                        book = book,
                        onOpenBook = onOpenBook,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ContinueReadingDetails(
    book: HomeBook,
    onOpenBook: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Continue Reading",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = book.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.primary,
        )
        book.author?.let { author ->
            Text(
                text = author,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        book.progressionPercent?.let { progress ->
            ProgressBadge(progress = progress)
        }
        Button(onClick = { onOpenBook(book.id) }) {
            Text("Open Book")
        }
    }
}

@Composable
private fun SectionHeader(
    eyebrow: String,
    title: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = eyebrow.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
private fun RecentImportsRow(
    books: List<HomeBook>,
    onOpenBook: (Long) -> Unit,
    onDeleteBook: (HomeBook) -> Unit,
) {
    LazyRow(
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag("recent-imports-row"),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        lazyRowItems(
            items = books,
            key = { book -> book.id },
        ) { book ->
            BookCard(
                book = book,
                modifier = Modifier.width(150.dp),
                onOpenBook = onOpenBook,
                onDeleteBook = onDeleteBook,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookCard(
    book: HomeBook,
    modifier: Modifier = Modifier,
    onOpenBook: (Long) -> Unit,
    onDeleteBook: (HomeBook) -> Unit,
) {
    var actionsExpanded by remember { mutableStateOf(false) }

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onOpenBook(book.id) },
                    onLongClick = { onDeleteBook(book) },
                ).testTag("book-card-${book.id}"),
        shape = MaterialTheme.shapes.extraLarge,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box {
                BookCover(
                    coverPath = book.coverPath,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f),
                )

                Surface(
                    modifier = Modifier.align(Alignment.TopEnd),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    tonalElevation = 4.dp,
                ) {
                    IconButton(
                        onClick = { actionsExpanded = true },
                        modifier = Modifier.testTag("book-actions-button-${book.id}"),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreHoriz,
                            contentDescription = "Actions for ${book.title}",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                DropdownMenu(
                    expanded = actionsExpanded,
                    onDismissRequest = { actionsExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Remove from shelf") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            actionsExpanded = false
                            onDeleteBook(book)
                        },
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = book.author ?: "Unknown author",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                book.progressionPercent?.takeIf { it > 0f }?.let { progress ->
                    ProgressBadge(progress = progress)
                }
            }
        }
    }
}

@Composable
private fun BookCover(
    coverPath: String?,
    modifier: Modifier = Modifier,
) {
    val coverFile =
        remember(coverPath) {
            coverPath
                ?.takeIf(String::isNotBlank)
                ?.let(::File)
                ?.takeIf(File::isFile)
        }

    Box(
        modifier =
            modifier
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        BookCoverPlaceholder(modifier = Modifier.matchParentSize())

        coverFile?.let { file ->
            AsyncImage(
                model = file,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun BookCoverPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier.background(
                brush =
                    Brush.linearGradient(
                        colors =
                            listOf(
                                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.88f),
                                MaterialTheme.colorScheme.primaryContainer,
                            ),
                    ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(44.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun ProgressBadge(progress: Float) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoStories,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = "${(progress * 100).roundToInt()}% read",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun DeleteBookDialog(
    book: HomeBook,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                contentDescription = null,
            )
        },
        title = { Text("Remove from shelf?") },
        text = {
            Text("InkFold will remove ${book.title} and delete its local EPUB copy.")
        },
        confirmButton = {
            FilledTonalButton(onClick = onConfirm) {
                Text("Remove")
            }
        },
        dismissButton = {
            FilledTonalButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
