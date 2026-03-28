package com.shubhamghanmode.inkfold.feature.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shubhamghanmode.inkfold.ui.theme.InkFoldTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyShelfStateShowsOnboardingCard() {
        composeRule.setContent {
            InkFoldTheme(darkTheme = false) {
                HomeScreen(
                    uiState = HomeUiState(
                        continueReading = null,
                        recentImports = emptyList(),
                        allBooks = emptyList(),
                        isEmpty = true,
                        transientMessage = null
                    ),
                    onImportClick = {},
                    onOpenBook = {},
                    onDeleteBook = {},
                    onTransientMessageShown = {}
                )
            }
        }

        composeRule.onNodeWithTag("import-button").assertIsDisplayed()
        composeRule.onNodeWithTag("empty-library-card").assertIsDisplayed()
    }

    @Test
    fun populatedShelfStateShowsContinueReadingAndBookShelves() {
        composeRule.setContent {
            InkFoldTheme(darkTheme = false) {
                HomeScreen(
                    uiState = populatedUiState(),
                    onImportClick = {},
                    onOpenBook = {},
                    onDeleteBook = {},
                    onTransientMessageShown = {}
                )
            }
        }

        composeRule.onNodeWithTag("continue-reading-card").assertIsDisplayed()
        composeRule.onNodeWithTag("recent-imports-row").assertIsDisplayed()
        composeRule.onNodeWithTag("book-card-1").assertIsDisplayed()
    }

    @Test
    fun tappingBookCardDispatchesOpenRequest() {
        var openedBookId: Long? = null

        composeRule.setContent {
            InkFoldTheme(darkTheme = false) {
                HomeScreen(
                    uiState = populatedUiState(),
                    onImportClick = {},
                    onOpenBook = { openedBookId = it },
                    onDeleteBook = {},
                    onTransientMessageShown = {}
                )
            }
        }

        composeRule.onNodeWithTag("book-card-1").performClick()

        assertEquals(1L, openedBookId)
    }

    @Test
    fun shelfWithoutCurrentReadShowsLibraryStatusInsteadOfEmptyState() {
        composeRule.setContent {
            InkFoldTheme(darkTheme = false) {
                HomeScreen(
                    uiState = populatedUiState().copy(continueReading = null),
                    onImportClick = {},
                    onOpenBook = {},
                    onDeleteBook = {},
                    onTransientMessageShown = {}
                )
            }
        }

        composeRule.onNodeWithTag("no-current-read-card").assertIsDisplayed()
        composeRule.onAllNodesWithTag("empty-library-card").assertCountEquals(0)
    }

    @Test
    fun visibleBookActionsAllowRemovingABook() {
        var deletedBookId: Long? = null

        composeRule.setContent {
            InkFoldTheme(darkTheme = false) {
                HomeScreen(
                    uiState = populatedUiState(),
                    onImportClick = {},
                    onOpenBook = {},
                    onDeleteBook = { deletedBookId = it },
                    onTransientMessageShown = {}
                )
            }
        }

        composeRule.onNodeWithTag("book-actions-button-1").performClick()
        composeRule.onNodeWithText("Remove from shelf").performClick()
        composeRule.onNodeWithText("Remove").performClick()

        assertEquals(1L, deletedBookId)
    }

    private fun populatedUiState(): HomeUiState =
        HomeUiState(
            continueReading = HomeBook(
                id = 1L,
                title = "Northanger Abbey",
                author = "Jane Austen",
                coverPath = null,
                progressionPercent = 0.48f
            ),
            recentImports = listOf(
                HomeBook(
                    id = 2L,
                    title = "The Left Hand of Darkness",
                    author = "Ursula K. Le Guin",
                    coverPath = null,
                    progressionPercent = null
                )
            ),
            allBooks = listOf(
                HomeBook(
                    id = 1L,
                    title = "Northanger Abbey",
                    author = "Jane Austen",
                    coverPath = null,
                    progressionPercent = 0.48f
                ),
                HomeBook(
                    id = 2L,
                    title = "The Left Hand of Darkness",
                    author = null,
                    coverPath = null,
                    progressionPercent = null
                )
            ),
            isEmpty = false,
            transientMessage = null
        )
}
