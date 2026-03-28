package com.shubhamghanmode.inkfold.feature.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shubhamghanmode.inkfold.AppContainer
import com.shubhamghanmode.inkfold.data.book.BookEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val appContainer: AppContainer
) : ViewModel() {
    private val transientMessage = MutableStateFlow<String?>(null)
    private val openBookRequestFlow = MutableSharedFlow<Long>(extraBufferCapacity = 1)

    val openBookRequests = openBookRequestFlow.asSharedFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        appContainer.libraryRepository.observeContinueReading(),
        appContainer.libraryRepository.observeRecentImports(),
        appContainer.libraryRepository.observeAllBooks(),
        appContainer.libraryRepository.observeBookCount(),
        transientMessage
    ) { continueReading, recentImports, allBooks, bookCount, message ->
        HomeUiState(
            continueReading = continueReading?.toHomeBook(),
            recentImports = recentImports.map(BookEntity::toHomeBook),
            allBooks = allBooks.map(BookEntity::toHomeBook),
            isEmpty = bookCount == 0,
            transientMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(
            continueReading = null,
            recentImports = emptyList(),
            allBooks = emptyList(),
            isEmpty = true,
            transientMessage = null
        )
    )

    fun importFromUri(uri: Uri) {
        viewModelScope.launch {
            appContainer.libraryRepository.importFromUri(uri)
                .onSuccess { bookId ->
                    openBook(bookId)
                }
                .onFailure { error ->
                    showTransientMessage(error.message ?: "InkFold could not import that EPUB.")
                }
        }
    }

    fun openBook(bookId: Long) {
        viewModelScope.launch {
            appContainer.readerRepository.prepare(bookId)
                .onSuccess {
                    appContainer.libraryRepository.markBookOpened(bookId)
                    openBookRequestFlow.emit(bookId)
                }
                .onFailure { error ->
                    showTransientMessage(error.message ?: "InkFold could not open that book.")
                }
        }
    }

    fun deleteBook(bookId: Long) {
        viewModelScope.launch {
            appContainer.libraryRepository.deleteBook(bookId)
                .onFailure { error ->
                    showTransientMessage(error.message ?: "InkFold could not remove that book.")
                }
        }
    }

    fun showTransientMessage(message: String) {
        transientMessage.value = message
    }

    fun consumeTransientMessage() {
        transientMessage.value = null
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    HomeViewModel(appContainer) as T
            }
    }
}

private fun BookEntity.toHomeBook(): HomeBook =
    HomeBook(
        id = id,
        title = title,
        author = author,
        coverPath = coverPath,
        progressionPercent = progressionPercent
    )
