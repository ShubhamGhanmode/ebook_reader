package com.shubhamghanmode.inkfold.feature.reader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shubhamghanmode.inkfold.InkFoldApplication
import com.shubhamghanmode.inkfold.R
import com.shubhamghanmode.inkfold.feature.reader.outline.ReaderOutlineMapper
import com.shubhamghanmode.inkfold.feature.reader.outline.ReaderOutlineUiState
import com.shubhamghanmode.inkfold.feature.reader.preferences.ReaderAppearanceScope
import com.shubhamghanmode.inkfold.feature.reader.preferences.ReaderAppearanceUiState
import com.shubhamghanmode.inkfold.feature.reader.preferences.ReaderImageFilterOption
import com.shubhamghanmode.inkfold.feature.reader.preferences.ReaderPageMarginPreset
import com.shubhamghanmode.inkfold.feature.reader.preferences.ReaderPreferenceNormalizer
import com.shubhamghanmode.inkfold.feature.reader.preferences.ReaderScrollModeOption
import com.shubhamghanmode.inkfold.feature.reader.preferences.ReaderThemeOption
import com.shubhamghanmode.inkfold.feature.reader.preferences.ReaderTypefaceOption
import com.shubhamghanmode.inkfold.feature.reader.tts.ReaderTtsError
import com.shubhamghanmode.inkfold.feature.reader.tts.ReaderTtsUiState
import com.shubhamghanmode.inkfold.feature.reader.tts.ReaderTtsViewModel
import kotlin.math.roundToInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.epub.EpubLayout
import org.readium.r2.shared.publication.services.locateProgression

data class ReaderUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val errorMessage: String? = null,
    val isChromeVisible: Boolean = true,
    val isSettingsVisible: Boolean = false,
    val canOpenSettings: Boolean = false,
    val appearance: ReaderAppearanceUiState = ReaderAppearanceUiState(),
    val navigation: ReaderNavigationUiState = ReaderNavigationUiState(),
    val tts: ReaderTtsUiState = ReaderTtsUiState(),
    val pageFlipOverlayState: PageFlipOverlayState = PageFlipOverlayState.Disabled
)

@OptIn(ExperimentalReadiumApi::class, ExperimentalCoroutinesApi::class)
class ReaderViewModel(
    application: Application,
    private val bookId: Long
) : AndroidViewModel(application) {
    private val appContainer get() = getApplication<InkFoldApplication>().appContainer
    private val initialSession = appContainer.readerRepository.get(bookId)
    private val pageFlipCoordinator = PageFlipCoordinator()
    private val initialOutlineState = initialSession?.let(::createOutlineUiState) ?: ReaderOutlineUiState()

    private val sessionState = MutableStateFlow(initialSession)
    private val loadingState = MutableStateFlow(initialSession == null)
    private val errorMessageState = MutableStateFlow<String?>(null)
    private val chromeVisibleState = MutableStateFlow(true)
    private val settingsVisibleState = MutableStateFlow(false)
    private val currentLocatorState = MutableStateFlow(initialSession?.initialLocator)
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
        initialSession?.let { session ->
            createAppearanceUiState(
                session = session,
                sharedPreferences = ReaderPreferenceNormalizer.normalize(session.initialSharedPreferences)
            )
        } ?: ReaderAppearanceUiState()
    )
    private val outlineState = MutableStateFlow(initialOutlineState)
    private val navigationRequestsFlow = MutableSharedFlow<Locator>(extraBufferCapacity = 1)
    private val ttsStartRequestsFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val ttsViewModelState = MutableStateFlow(
        initialSession?.ttsInitData?.let(::createTtsViewModel)
    )
    private var outlineLocatorsByItemId: Map<String, Locator> =
        initialSession?.let { session ->
            ReaderOutlineMapper.map(session.publication).locatorsByItemId
        }.orEmpty()
    private var preferencesBindingJob: Job? = null
    private var persistSharedPreferencesJob: Job? = null

    val session: StateFlow<ReaderSession?> = sessionState.asStateFlow()
    val navigatorPreferences: StateFlow<EpubPreferences> = navigatorPreferencesState.asStateFlow()
    val navigationRequests: SharedFlow<Locator> = navigationRequestsFlow.asSharedFlow()
    val ttsStartRequests: SharedFlow<Unit> = ttsStartRequestsFlow.asSharedFlow()
    val ttsHighlight: StateFlow<Locator?> = ttsViewModelState.flatMapLatest { ttsViewModel ->
        ttsViewModel?.highlight ?: flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val ttsVisualSyncLocators: Flow<Locator> = ttsViewModelState.flatMapLatest { ttsViewModel ->
        ttsViewModel?.visualSyncLocator ?: emptyFlow()
    }
    val ttsErrors: Flow<ReaderTtsError> = ttsViewModelState.flatMapLatest { ttsViewModel ->
        ttsViewModel?.errors ?: emptyFlow()
    }
    private val ttsUiState = ttsViewModelState.flatMapLatest { ttsViewModel ->
        ttsViewModel?.uiState ?: flowOf(ReaderTtsUiState())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ReaderTtsUiState())

    val uiState: StateFlow<ReaderUiState> = combine(
        loadingState,
        sessionState,
        errorMessageState,
        chromeVisibleState,
        settingsVisibleState,
        appearanceState,
        currentLocatorState,
        outlineState,
        pageFlipCoordinator.overlayState,
        ttsUiState
    ) { values ->
        val isLoading = values[0] as Boolean
        val session = values[1] as ReaderSession?
        val errorMessage = values[2] as String?
        val isChromeVisible = values[3] as Boolean
        val isSettingsVisible = values[4] as Boolean
        val appearance = values[5] as ReaderAppearanceUiState
        val currentLocator = values[6] as Locator?
        val outline = values[7] as ReaderOutlineUiState
        val pageFlipOverlayState = values[8] as PageFlipOverlayState
        val tts = values[9] as ReaderTtsUiState

        ReaderUiState(
            isLoading = isLoading,
            title = session?.title.orEmpty(),
            errorMessage = errorMessage,
            isChromeVisible = isChromeVisible,
            isSettingsVisible = isSettingsVisible,
            canOpenSettings = session != null && !isLoading,
            appearance = appearance,
            navigation = createNavigationUiState(session, currentLocator, outline),
            tts = tts,
            pageFlipOverlayState = pageFlipOverlayState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ReaderUiState(
            isLoading = loadingState.value,
            title = initialSession?.title.orEmpty(),
            errorMessage = null,
            isChromeVisible = chromeVisibleState.value,
            isSettingsVisible = settingsVisibleState.value,
            canOpenSettings = initialSession != null && !loadingState.value,
            appearance = appearanceState.value,
            navigation = createNavigationUiState(
                session = initialSession,
                locator = currentLocatorState.value,
                outline = outlineState.value
            ),
            tts = ttsUiState.value,
            pageFlipOverlayState = pageFlipCoordinator.overlayState.value
        )
    )

    init {
        initialSession?.let(::bindSession)

        if (initialSession == null) {
            loadSession()
        }
    }

    private fun loadSession() {
        viewModelScope.launch {
            appContainer.readerRepository.prepare(bookId)
                .onSuccess { session ->
                    appContainer.libraryRepository.markBookOpened(bookId)
                    sessionState.value = session
                    bindSession(session)
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
        currentLocatorState.value = locator
        viewModelScope.launch {
            appContainer.libraryRepository.saveProgression(bookId, locator)
        }
    }

    fun onReaderSurfaceTapped() {
        when {
            settingsVisibleState.value -> closeSettings()
            outlineState.value.isVisible -> closeOutline()
            else -> chromeVisibleState.value = !chromeVisibleState.value
        }
    }

    fun openSettings() {
        if (sessionState.value == null) {
            return
        }

        chromeVisibleState.value = true
        outlineState.value = outlineState.value.copy(isVisible = false)
        settingsVisibleState.value = true
    }

    fun closeSettings() {
        settingsVisibleState.value = false
    }

    fun openOutline() {
        if (outlineState.value.sections.isEmpty()) {
            return
        }

        chromeVisibleState.value = true
        settingsVisibleState.value = false
        outlineState.value = outlineState.value.copy(isVisible = true)
    }

    fun closeOutline() {
        outlineState.value = outlineState.value.copy(isVisible = false)
    }

    fun selectOutlineSection(index: Int) {
        outlineState.value = outlineState.value.copy(
            selectedSectionIndex = index.coerceIn(0, outlineState.value.sections.lastIndex)
        )
    }

    fun navigateToOutlineItem(itemId: String) {
        outlineLocatorsByItemId[itemId]?.let { locator ->
            outlineState.value = outlineState.value.copy(isVisible = false)
            navigateTo(locator)
        }
    }

    fun selectTheme(themeOption: ReaderThemeOption) {
        updateSharedPreferences(persistImmediately = true) { preferences ->
            preferences.copy(theme = themeOption.readiumTheme)
        }
    }

    fun selectScrollMode(scrollMode: ReaderScrollModeOption) {
        updateSharedPreferences(persistImmediately = true) { preferences ->
            preferences.copy(scroll = scrollMode.readiumValue)
        }
    }

    fun updateFontSize(fontSize: Float) {
        updateSharedPreferences { preferences ->
            preferences.copy(fontSize = fontSize.toDouble())
        }
    }

    fun increaseFontSize() {
        updateSharedPreferences(persistImmediately = true) { preferences ->
            preferences.copy(
                fontSize = (activeAppearanceState().textSize + activeAppearanceState().textSizeStep)
                    .toDouble()
            )
        }
    }

    fun decreaseFontSize() {
        updateSharedPreferences(persistImmediately = true) { preferences ->
            preferences.copy(
                fontSize = (activeAppearanceState().textSize - activeAppearanceState().textSizeStep)
                    .toDouble()
            )
        }
    }

    fun selectTypeface(typeface: ReaderTypefaceOption) {
        updateSharedPreferences(persistImmediately = true) { preferences ->
            preferences.copy(fontFamily = typeface.readiumFontFamily)
        }
    }

    fun selectImageFilter(imageFilter: ReaderImageFilterOption) {
        updateSharedPreferences(persistImmediately = true) { preferences ->
            preferences.copy(imageFilter = imageFilter.readiumImageFilter)
        }
    }

    fun selectPageMarginPreset(preset: ReaderPageMarginPreset) {
        updateSharedPreferences(persistImmediately = true) { preferences ->
            preferences.copy(pageMargins = preset.value)
        }
    }

    fun decreasePageMargins() {
        val presets = ReaderPageMarginPreset.entries
        val currentIndex = presets.indexOf(activeAppearanceState().pageMarginPreset)
        selectPageMarginPreset(presets[(currentIndex - 1).coerceAtLeast(0)])
    }

    fun increasePageMargins() {
        val presets = ReaderPageMarginPreset.entries
        val currentIndex = presets.indexOf(activeAppearanceState().pageMarginPreset)
        selectPageMarginPreset(presets[(currentIndex + 1).coerceAtMost(presets.lastIndex)])
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

        previewSharedPreferencesState.value = ReaderPreferenceNormalizer.normalize(
            session.preferencesManager.defaultSharedPreferences
        )
        persistSharedPreferencesJob?.cancel()
        persistSharedPreferencesJob = viewModelScope.launch {
            session.preferencesManager.reset(ReaderAppearanceScope.SharedDefaults)
        }
    }

    fun jumpToProgression(totalProgression: Float) {
        val session = sessionState.value ?: return
        viewModelScope.launch {
            session.publication.locateProgression(
                totalProgression.coerceIn(0f, 1f).toDouble()
            )?.let(::navigateTo)
        }
    }

    fun requestStartReadAloud() {
        val ttsViewModel = ttsViewModelState.value ?: return
        chromeVisibleState.value = true

        if (ttsUiState.value.isActive) {
            ttsViewModel.resume()
        } else {
            ttsStartRequestsFlow.tryEmit(Unit)
        }
    }

    fun startReadAloud(initialLocator: Locator?) {
        ttsViewModelState.value?.start(initialLocator)
    }

    fun toggleReadAloudPlayback() {
        ttsViewModelState.value?.togglePlayPause()
    }

    fun stopReadAloud() {
        ttsViewModelState.value?.stop()
    }

    fun readPreviousUtterance() {
        ttsViewModelState.value?.previous()
    }

    fun readNextUtterance() {
        ttsViewModelState.value?.next()
    }

    fun openReadAloudSettings() {
        chromeVisibleState.value = true
        ttsViewModelState.value?.openSettings()
    }

    fun closeReadAloudSettings() {
        ttsViewModelState.value?.closeSettings()
    }

    fun updateReadAloudSpeed(speed: Double) {
        ttsViewModelState.value?.updateSpeed(speed)
    }

    fun updateReadAloudPitch(pitch: Double) {
        ttsViewModelState.value?.updatePitch(pitch)
    }

    fun selectReadAloudLanguage(languageCode: String?) {
        ttsViewModelState.value?.updateLanguage(languageCode)
    }

    fun selectReadAloudVoice(voiceId: String?) {
        ttsViewModelState.value?.updateVoice(voiceId)
    }

    fun dismissMissingVoiceDataPrompt() {
        ttsViewModelState.value?.dismissMissingVoiceDataPrompt()
    }

    fun pauseReadAloudForBackground() {
        ttsViewModelState.value?.pauseForBackground()
    }

    override fun onCleared() {
        preferencesBindingJob?.cancel()
        persistSharedPreferencesJob?.cancel()
        ttsViewModelState.value?.stop()
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
                    val application = checkNotNull(
                        extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    )
                    return ReaderViewModel(application, bookId) as T
                }
            }
    }

    private fun bindSession(session: ReaderSession) {
        bindReaderPreferences(session)
        bindOutline(session)
        if (ttsViewModelState.value == null && session.ttsInitData != null) {
            ttsViewModelState.value = createTtsViewModel(session.ttsInitData)
        }
    }

    private fun bindReaderPreferences(session: ReaderSession) {
        preferencesBindingJob?.cancel()
        currentLocatorState.value = session.initialLocator
        storedSharedPreferencesState.value = session.initialSharedPreferences
        storedBookPreferencesState.value = session.initialBookPreferences
        navigatorPreferencesState.value =
            ReaderPreferenceNormalizer.normalize(session.initialSharedPreferences) +
                session.initialBookPreferences
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
                    activeSharedPreferences = ReaderPreferenceNormalizer.normalize(
                        previewSharedPreferences ?: sharedPreferences
                    )
                )
            }.collect { snapshot ->
                storedSharedPreferencesState.value = snapshot.sharedPreferences
                storedBookPreferencesState.value = snapshot.bookPreferences
                navigatorPreferencesState.value = snapshot.activeSharedPreferences + snapshot.bookPreferences
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

    private fun bindOutline(session: ReaderSession) {
        val mapping = ReaderOutlineMapper.map(session.publication)
        outlineLocatorsByItemId = mapping.locatorsByItemId
        outlineState.value = ReaderOutlineUiState(
            isVisible = false,
            sections = mapping.sections,
            selectedSectionIndex = outlineState.value.selectedSectionIndex
                .coerceAtMost((mapping.sections.lastIndex).coerceAtLeast(0))
        )
    }

    private fun navigateTo(locator: Locator) {
        viewModelScope.launch {
            ttsViewModelState.value?.go(locator)
            navigationRequestsFlow.emit(locator)
        }
    }

    private fun updateSharedPreferences(
        persistImmediately: Boolean = false,
        updater: (EpubPreferences) -> EpubPreferences
    ) {
        val updatedPreferences = ReaderPreferenceNormalizer.normalize(
            updater(activeSharedPreferences())
        )
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
        ReaderPreferenceNormalizer.normalize(
            previewSharedPreferencesState.value ?: storedSharedPreferencesState.value
        )

    private fun activeAppearanceState(): ReaderAppearanceUiState = appearanceState.value

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
        val resolvedScrollMode = ReaderScrollModeOption.from(
            preferencesEditor.scroll.value ?: preferencesEditor.scroll.effectiveValue
        )
        val resolvedTextSize = ReaderPreferenceNormalizer.clampTextSize(
            preferencesEditor.fontSize.value ?: preferencesEditor.fontSize.effectiveValue
        )
        val resolvedPageMargins = ReaderPreferenceNormalizer.snapPageMargins(
            preferencesEditor.pageMargins.value ?: preferencesEditor.pageMargins.effectiveValue
        )
        val resolvedTypeface = ReaderTypefaceOption.from(
            preferencesEditor.fontFamily.value ?: preferencesEditor.fontFamily.effectiveValue
        )
        val resolvedImageFilter = ReaderImageFilterOption.from(
            preferencesEditor.imageFilter.value ?: preferencesEditor.imageFilter.effectiveValue
        )

        return ReaderAppearanceUiState(
            supportsAppearanceControls = true,
            selectedTheme = resolvedTheme,
            isScrollModeVisible = preferencesEditor.scroll.isEffective,
            selectedScrollMode = resolvedScrollMode,
            textSize = resolvedTextSize.toFloat(),
            textSizeLabel = ReaderPreferenceNormalizer.formatTextSize(resolvedTextSize),
            canDecreaseTextSize = resolvedTextSize > ReaderPreferenceNormalizer.TextSizeMin,
            canIncreaseTextSize = resolvedTextSize < ReaderPreferenceNormalizer.TextSizeMax,
            selectedTypeface = resolvedTypeface,
            selectedImageFilter = resolvedImageFilter,
            isImageFilterEnabled = preferencesEditor.imageFilter.isEffective,
            imageFilterHelperMessageRes = if (preferencesEditor.imageFilter.isEffective) {
                null
            } else {
                R.string.reader_settings_image_filter_dark_only
            },
            pageMarginPreset = resolvedPageMargins,
            canDecreasePageMargins = resolvedPageMargins != ReaderPageMarginPreset.entries.first(),
            canIncreasePageMargins = resolvedPageMargins != ReaderPageMarginPreset.entries.last(),
            canReset = sharedPreferences != ReaderPreferenceNormalizer.normalize(
                session.preferencesManager.defaultSharedPreferences
            )
        )
    }

    private fun createNavigationUiState(
        session: ReaderSession?,
        locator: Locator?,
        outline: ReaderOutlineUiState
    ): ReaderNavigationUiState {
        val totalPositions = session?.positionCount ?: 0
        if (session == null || totalPositions <= 1) {
            return ReaderNavigationUiState(outline = outline)
        }

        val currentProgression = resolveCurrentProgression(locator, totalPositions)
        val currentPosition = resolveCurrentPosition(locator, currentProgression, totalPositions)

        return ReaderNavigationUiState(
            isEnabled = true,
            currentProgression = currentProgression,
            currentPosition = currentPosition,
            totalPositions = totalPositions,
            outline = outline
        )
    }

    private fun resolveCurrentProgression(
        locator: Locator?,
        totalPositions: Int
    ): Float {
        val progression = locator?.locations?.totalProgression
            ?: locator?.locations?.position
                ?.takeIf { totalPositions > 1 }
                ?.let { position -> (position - 1).toFloat() / (totalPositions - 1).toFloat() }
            ?: 0f

        return progression.toFloat().coerceIn(0f, 1f)
    }

    private fun resolveCurrentPosition(
        locator: Locator?,
        currentProgression: Float,
        totalPositions: Int
    ): Int =
        locator?.locations?.position
            ?.coerceIn(1, totalPositions)
            ?: (1 + (currentProgression * (totalPositions - 1)).roundToInt())
                .coerceIn(1, totalPositions)

    private fun createOutlineUiState(session: ReaderSession): ReaderOutlineUiState {
        val mapping = ReaderOutlineMapper.map(session.publication)
        outlineLocatorsByItemId = mapping.locatorsByItemId
        return ReaderOutlineUiState(
            isVisible = false,
            sections = mapping.sections,
            selectedSectionIndex = 0
        )
    }

    private fun createTtsViewModel(
        initData: com.shubhamghanmode.inkfold.feature.reader.tts.ReaderTtsInitData
    ): ReaderTtsViewModel =
        ReaderTtsViewModel(
            application = getApplication(),
            viewModelScope = viewModelScope,
            initData = initData
        )

    private data class ReaderPreferencesSnapshot(
        val sharedPreferences: EpubPreferences,
        val bookPreferences: EpubPreferences,
        val activeSharedPreferences: EpubPreferences
    )
}
