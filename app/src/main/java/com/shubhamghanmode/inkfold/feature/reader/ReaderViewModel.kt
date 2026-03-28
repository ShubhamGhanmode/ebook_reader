package com.shubhamghanmode.inkfold.feature.reader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shubhamghanmode.inkfold.InkFoldApplication
import com.shubhamghanmode.inkfold.R
import com.shubhamghanmode.inkfold.feature.reader.preferences.ReaderAppearanceScope
import com.shubhamghanmode.inkfold.feature.reader.preferences.ReaderAppearanceUiState
import com.shubhamghanmode.inkfold.feature.reader.preferences.ReaderThemeOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.publication.epub.EpubLayout
import org.readium.r2.shared.publication.Locator

data class ReaderUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val session: ReaderSession? = null,
    val errorMessage: String? = null,
    val isChromeVisible: Boolean = true,
    val isSettingsVisible: Boolean = false,
    val appearance: ReaderAppearanceUiState = ReaderAppearanceUiState(),
    val pageFlipOverlayState: PageFlipOverlayState = PageFlipOverlayState.Disabled
)

@OptIn(ExperimentalReadiumApi::class)
class ReaderViewModel(
    application: Application,
    private val bookId: Long
) : AndroidViewModel(application) {
    private val appContainer get() = getApplication<InkFoldApplication>().appContainer
    private val initialSession = appContainer.readerRepository.get(bookId)
    private val pageFlipCoordinator = PageFlipCoordinator()

    private val sessionState = MutableStateFlow(initialSession)
    private val loadingState = MutableStateFlow(initialSession == null)
    private val errorMessageState = MutableStateFlow<String?>(null)
    private val chromeVisibleState = MutableStateFlow(true)
    private val settingsVisibleState = MutableStateFlow(false)
    private val storedSharedPreferencesState = MutableStateFlow(
        initialSession?.initialSharedPreferences ?: EpubPreferences()
    )
    private val previewSharedPreferencesState = MutableStateFlow<EpubPreferences?>(null)
    private val storedBookPreferencesState = MutableStateFlow(
        initialSession?.initialBookPreferences ?: EpubPreferences()
    )
    private val navigatorPreferencesState = MutableStateFlow(
        initialSession?.initialPreferences ?: EpubPreferences()
    )
    private val appearanceState = MutableStateFlow(
        initialSession?.let {
            createAppearanceUiState(
                session = it,
                sharedPreferences = it.initialSharedPreferences
            )
        } ?: ReaderAppearanceUiState()
    )
    private var preferencesBindingJob: Job? = null
    private var persistSharedPreferencesJob: Job? = null

    val navigatorPreferences: StateFlow<EpubPreferences> = navigatorPreferencesState.asStateFlow()

    val uiState: StateFlow<ReaderUiState> = combine(
        loadingState,
        sessionState,
        errorMessageState,
        chromeVisibleState,
        settingsVisibleState,
        appearanceState,
        pageFlipCoordinator.overlayState
    ) { values ->
        val isLoading = values[0] as Boolean
        val session = values[1] as ReaderSession?
        val errorMessage = values[2] as String?
        val isChromeVisible = values[3] as Boolean
        val isSettingsVisible = values[4] as Boolean
        val appearance = values[5] as ReaderAppearanceUiState
        val pageFlipOverlayState = values[6] as PageFlipOverlayState

        ReaderUiState(
            isLoading = isLoading,
            title = session?.title.orEmpty(),
            session = session,
            errorMessage = errorMessage,
            isChromeVisible = isChromeVisible,
            isSettingsVisible = isSettingsVisible,
            appearance = appearance,
            pageFlipOverlayState = pageFlipOverlayState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ReaderUiState(
            isLoading = loadingState.value,
            title = initialSession?.title.orEmpty(),
            session = initialSession,
            errorMessage = null,
            isChromeVisible = chromeVisibleState.value,
            isSettingsVisible = settingsVisibleState.value,
            appearance = appearanceState.value,
            pageFlipOverlayState = pageFlipCoordinator.overlayState.value
        )
    )

    init {
        initialSession?.let(::bindReaderPreferences)

        if (initialSession == null) {
            loadSession()
        }
    }

    private fun loadSession() {
        viewModelScope.launch {
            appContainer.readerRepository.prepare(bookId)
                .onSuccess { session ->
                    appContainer.libraryRepository.markBookOpened(bookId)
                    bindReaderPreferences(session)
                    sessionState.value = session
                    loadingState.value = false
                    errorMessageState.value = null
                }
                .onFailure { error ->
                    loadingState.value = false
                    errorMessageState.value = error.message ?: "InkFold could not open that book."
                }
        }
    }

    fun saveProgression(locator: Locator) {
        viewModelScope.launch {
            appContainer.libraryRepository.saveProgression(bookId, locator)
        }
    }

    fun onReaderSurfaceTapped() {
        if (settingsVisibleState.value) {
            closeSettings()
            return
        }

        chromeVisibleState.value = !chromeVisibleState.value
    }

    fun openSettings() {
        if (sessionState.value == null) {
            return
        }

        chromeVisibleState.value = true
        settingsVisibleState.value = true
    }

    fun closeSettings() {
        settingsVisibleState.value = false
    }

    fun selectTheme(themeOption: ReaderThemeOption) {
        updateSharedPreferences(
            persistImmediately = true
        ) { preferences ->
            preferences.copy(theme = themeOption.readiumTheme)
        }
    }

    fun updateFontSize(fontSize: Float) {
        updateSharedPreferences { preferences ->
            preferences.copy(fontSize = fontSize.toDouble())
        }
    }

    fun updatePageMargins(pageMargins: Float) {
        updateSharedPreferences { preferences ->
            preferences.copy(pageMargins = pageMargins.toDouble())
        }
    }

    fun flushAppearanceChanges() {
        val previewPreferences = previewSharedPreferencesState.value ?: return
        persistSharedPreferencesJob?.cancel()
        persistSharedPreferencesJob = viewModelScope.launch {
            persistSharedPreferences(previewPreferences)
        }
    }

    fun resetAppearance() {
        val session = sessionState.value ?: return

        previewSharedPreferencesState.value = session.preferencesManager.defaultSharedPreferences
        persistSharedPreferencesJob?.cancel()
        persistSharedPreferencesJob = viewModelScope.launch {
            session.preferencesManager.reset(ReaderAppearanceScope.SharedDefaults)
        }
    }

    override fun onCleared() {
        preferencesBindingJob?.cancel()
        persistSharedPreferencesJob?.cancel()
        appContainer.readerRepository.close(bookId)
    }

    companion object {
        private const val APPEARANCE_PERSIST_DEBOUNCE_MS = 120L

        fun factory(bookId: Long): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: androidx.lifecycle.viewmodel.CreationExtras
                ): T {
                    val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                    return ReaderViewModel(application, bookId) as T
                }
            }
    }

    private fun bindReaderPreferences(session: ReaderSession) {
        preferencesBindingJob?.cancel()
        storedSharedPreferencesState.value = session.initialSharedPreferences
        storedBookPreferencesState.value = session.initialBookPreferences
        navigatorPreferencesState.value = session.initialPreferences
        appearanceState.value = createAppearanceUiState(
            session = session,
            sharedPreferences = activeSharedPreferences()
        )

        preferencesBindingJob = viewModelScope.launch {
            combine(
                session.preferencesManager.sharedPreferences,
                session.preferencesManager.bookPreferences,
                previewSharedPreferencesState
            ) { sharedPreferences, bookPreferences, previewSharedPreferences ->
                ReaderPreferencesSnapshot(
                    sharedPreferences = sharedPreferences,
                    bookPreferences = bookPreferences,
                    activeSharedPreferences = previewSharedPreferences ?: sharedPreferences
                )
            }.collectLatest { snapshot ->
                storedSharedPreferencesState.value = snapshot.sharedPreferences
                storedBookPreferencesState.value = snapshot.bookPreferences
                navigatorPreferencesState.value =
                    snapshot.activeSharedPreferences + snapshot.bookPreferences
                appearanceState.value = createAppearanceUiState(
                    session = session,
                    sharedPreferences = snapshot.activeSharedPreferences
                )

                if (previewSharedPreferencesState.value == snapshot.sharedPreferences) {
                    previewSharedPreferencesState.value = null
                }
            }
        }
    }

    private fun updateSharedPreferences(
        persistImmediately: Boolean = false,
        updater: (EpubPreferences) -> EpubPreferences
    ) {
        val updatedPreferences = updater(activeSharedPreferences())
        previewSharedPreferencesState.value = updatedPreferences

        persistSharedPreferencesJob?.cancel()
        persistSharedPreferencesJob = viewModelScope.launch {
            if (!persistImmediately) {
                delay(APPEARANCE_PERSIST_DEBOUNCE_MS)
            }

            persistSharedPreferences(updatedPreferences)
        }
    }

    private suspend fun persistSharedPreferences(preferences: EpubPreferences) {
        sessionState.value
            ?.preferencesManager
            ?.setPreferences(ReaderAppearanceScope.SharedDefaults, preferences)
    }

    private fun activeSharedPreferences(): EpubPreferences =
        previewSharedPreferencesState.value ?: storedSharedPreferencesState.value

    private fun createAppearanceUiState(
        session: ReaderSession,
        sharedPreferences: EpubPreferences
    ): ReaderAppearanceUiState {
        val preferencesEditor = session.navigatorFactory.createPreferencesEditor(sharedPreferences)

        if (preferencesEditor.layout != EpubLayout.REFLOWABLE) {
            return ReaderAppearanceUiState(
                canReset = sharedPreferences != session.preferencesManager.defaultSharedPreferences,
                helperMessageRes = R.string.reader_settings_fixed_layout_message
            )
        }

        val resolvedTheme = ReaderThemeOption.from(
            preferencesEditor.theme.value ?: preferencesEditor.theme.effectiveValue
        )
        val resolvedFontSize = (preferencesEditor.fontSize.value
            ?: preferencesEditor.fontSize.effectiveValue).toFloat()
        val resolvedPageMargins = (preferencesEditor.pageMargins.value
            ?: preferencesEditor.pageMargins.effectiveValue).toFloat()

        return ReaderAppearanceUiState(
            supportsAppearanceControls = true,
            selectedTheme = resolvedTheme,
            fontSize = resolvedFontSize,
            fontSizeLabel = preferencesEditor.fontSize.formatValue(resolvedFontSize.toDouble()),
            fontSizeRange = preferencesEditor.fontSize.supportedRange.run {
                start.toFloat()..endInclusive.toFloat()
            },
            pageMargins = resolvedPageMargins,
            pageMarginsLabel = preferencesEditor.pageMargins.formatValue(resolvedPageMargins.toDouble()),
            pageMarginsRange = preferencesEditor.pageMargins.supportedRange.run {
                start.toFloat()..endInclusive.toFloat()
            },
            canReset = sharedPreferences != session.preferencesManager.defaultSharedPreferences
        )
    }

    private data class ReaderPreferencesSnapshot(
        val sharedPreferences: EpubPreferences,
        val bookPreferences: EpubPreferences,
        val activeSharedPreferences: EpubPreferences
    )
}
