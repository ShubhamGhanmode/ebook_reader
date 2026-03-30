package com.shubhamghanmode.inkfold.feature.reader.tts

import android.app.Application
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.readium.navigator.media.tts.AndroidTtsNavigator
import org.readium.navigator.media.tts.TtsNavigator
import org.readium.navigator.media.tts.android.AndroidTtsEngine
import org.readium.navigator.media.tts.android.AndroidTtsPreferences
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.Language
import org.readium.r2.shared.util.getOrElse

@OptIn(ExperimentalReadiumApi::class, ExperimentalCoroutinesApi::class)
class ReaderTtsViewModel(
    private val application: Application,
    private val viewModelScope: CoroutineScope,
    private val initData: ReaderTtsInitData
) : TtsNavigator.Listener {
    private val navigatorState = MutableStateFlow<AndroidTtsNavigator?>(null)
    private val settingsVisibleState = MutableStateFlow(false)
    private val missingVoiceDataLanguageState = MutableStateFlow<Language?>(null)
    private val _errors = MutableSharedFlow<ReaderTtsError>(extraBufferCapacity = 1)
    private var launchJob: Job? = null

    private val preferencesState = initData.preferencesManager.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AndroidTtsPreferences()
    )
    private val playbackState = navigatorState.flatMapLatest { navigator: AndroidTtsNavigator? ->
        navigator?.playback ?: flowOf<TtsNavigator.Playback?>(null)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )
    private val locationState = navigatorState.flatMapLatest { navigator: AndroidTtsNavigator? ->
        navigator?.location ?: flowOf<TtsNavigator.Location?>(null)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val errors = _errors.asSharedFlow()

    val highlight: StateFlow<Locator?> = locationState
        .map { location: TtsNavigator.Location? -> location?.utteranceLocator }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val visualSyncLocator: Flow<Locator> = locationState
        .map { location: TtsNavigator.Location? ->
            location?.tokenLocator ?: location?.utteranceLocator
        }
        .filterNotNull()

    private val snapshotState = combine(
        preferencesState,
        navigatorState,
        playbackState,
        locationState,
        settingsVisibleState
    ) { preferences, navigator, playback, location, isSettingsVisible ->
        ReaderTtsSnapshot(
            preferences = preferences,
            navigator = navigator,
            playback = playback,
            location = location,
            isSettingsVisible = isSettingsVisible
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ReaderTtsSnapshot()
    )

    val uiState: StateFlow<ReaderTtsUiState> = combine(
        snapshotState,
        missingVoiceDataLanguageState
    ) { snapshot, missingVoiceDataLanguage ->
        val preferences = snapshot.preferences
        val navigator = snapshot.navigator
        val playback = snapshot.playback
        val location = snapshot.location
        val editor = initData.navigatorFactory.createPreferencesEditor(preferences)
        val speed = editor.speed.value ?: editor.speed.effectiveValue
        val pitch = editor.pitch.value ?: editor.pitch.effectiveValue
        val selectedLanguage = (editor.language.value ?: editor.language.effectiveValue)?.removeRegion()
        val supportedLanguages = navigator
            ?.voices
            .orEmpty()
            .map { voice -> voice.language.removeRegion() }
            .distinctBy(Language::code)
            .sortedBy { language ->
                language.locale.getDisplayLanguage(Locale.getDefault())
            }

        val availableLanguages = buildList {
            add(
                ReaderTtsLanguageOption(
                    code = null,
                    label = bookDefaultLanguageLabel(editor.language.effectiveValue)
                )
            )
            addAll(
                supportedLanguages.map { language ->
                    ReaderTtsLanguageOption(
                        code = language.code,
                        label = language.displayName()
                    )
                }
            )
        }

        val selectedVoiceId = selectedLanguage
            ?.let { language -> editor.voices.effectiveValue[language]?.value }
        val availableVoices = buildList {
            add(
                ReaderTtsVoiceOption(
                    id = null,
                    label = application.getString(
                        com.shubhamghanmode.inkfold.R.string.reader_tts_voice_system_default
                    )
                )
            )
            addAll(
                navigator
                    ?.voices
                    .orEmpty()
                    .filter { voice -> voice.language.removeRegion() == selectedLanguage }
                    .sortedWith(
                        compareByDescending<AndroidTtsEngine.Voice> { it.quality.ordinal }
                            .thenBy { it.id.value.lowercase(Locale.getDefault()) }
                    )
                    .map { voice ->
                        ReaderTtsVoiceOption(
                            id = voice.id.value,
                            label = voice.id.value,
                            supportingLabel = if (voice.requiresNetwork) {
                                application.getString(
                                    com.shubhamghanmode.inkfold.R.string.reader_tts_voice_network
                                )
                            } else {
                                null
                            }
                        )
                    }
            )
        }

        ReaderTtsUiState(
            isSupported = true,
            isActive = navigator != null,
            isPlaying = playback?.playWhenReady == true,
            hasHighlight = location != null,
            canOpenSettings = navigator != null,
            canSkipPrevious = navigator?.hasPreviousUtterance() == true,
            canSkipNext = navigator?.hasNextUtterance() == true,
            currentUtterance = location?.utterance,
            currentLocationLabel = location?.utteranceLocator?.title ?: location?.href.toString(),
            isSettingsVisible = navigator != null && snapshot.isSettingsVisible,
            speed = speed,
            speedLabel = editor.speed.formatValue(speed),
            pitch = pitch,
            pitchLabel = editor.pitch.formatValue(pitch),
            selectedLanguageLabel = editor.language.value?.displayName()
                ?: bookDefaultLanguageLabel(editor.language.effectiveValue),
            availableLanguages = availableLanguages,
            selectedVoiceLabel = availableVoices
                .firstOrNull { option -> option.id == selectedVoiceId }
                ?.label
                ?: application.getString(
                    com.shubhamghanmode.inkfold.R.string.reader_tts_voice_system_default
                ),
            availableVoices = availableVoices,
            missingVoiceDataLanguageLabel = missingVoiceDataLanguage?.displayName()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ReaderTtsUiState(isSupported = true)
    )

    init {
        preferencesState
            .map { preferences -> preferences.copy(voices = preferences.voices?.ifEmpty { null }) }
            .onEach { preferences ->
                navigatorState.value?.submitPreferences(preferences)
            }
            .launchIn(viewModelScope)

        playbackState
            .map { playback -> playback?.state as? TtsNavigator.State }
            .onEach { state ->
                when (state) {
                    is TtsNavigator.State.Ended -> stop()
                    is TtsNavigator.State.Failure -> onPlaybackError(state.error)
                    null, TtsNavigator.State.Ready -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    fun start(initialLocator: Locator?) {
        navigatorState.value?.let { existingNavigator ->
            existingNavigator.play()
            return
        }

        if (launchJob != null) {
            return
        }

        launchJob = viewModelScope.launch {
            val navigator = initData.navigatorFactory.createNavigator(
                listener = this@ReaderTtsViewModel,
                initialLocator = initialLocator,
                initialPreferences = preferencesState.value
            ).getOrElse { error ->
                val mappedError = when (error) {
                    is org.readium.navigator.media.tts.TtsNavigatorFactory.Error.EngineInitialization ->
                        ReaderTtsError.InitializationFailed

                    is org.readium.navigator.media.tts.TtsNavigatorFactory.Error.UnsupportedPublication ->
                        ReaderTtsError.UnsupportedPublication
                }
                _errors.tryEmit(mappedError)
                launchJob = null
                return@launch
            }

            navigatorState.value = navigator
            launchJob = null
            navigator.play()
        }
    }

    fun stop() {
        launchJob?.cancel()
        launchJob = null
        navigatorState.value?.close()
        navigatorState.value = null
        settingsVisibleState.value = false
    }

    fun pause() {
        navigatorState.value?.pause()
    }

    fun resume() {
        navigatorState.value?.play()
    }

    fun togglePlayPause() {
        if (playbackState.value?.playWhenReady == true) {
            pause()
        } else {
            resume()
        }
    }

    fun previous() {
        navigatorState.value?.skipToPreviousUtterance()
    }

    fun next() {
        navigatorState.value?.skipToNextUtterance()
    }

    fun go(locator: Locator) {
        navigatorState.value?.go(locator)
    }

    fun openSettings() {
        if (navigatorState.value != null) {
            settingsVisibleState.value = true
        }
    }

    fun closeSettings() {
        settingsVisibleState.value = false
    }

    fun dismissMissingVoiceDataPrompt() {
        missingVoiceDataLanguageState.value = null
    }

    fun pauseForBackground() {
        if (playbackState.value?.playWhenReady == true) {
            pause()
        }
    }

    fun updateSpeed(speed: Double) {
        updatePreferences { current ->
            current.copy(speed = speed.coerceIn(0.5, 2.0))
        }
    }

    fun updatePitch(pitch: Double) {
        updatePreferences { current ->
            current.copy(pitch = pitch.coerceIn(0.5, 2.0))
        }
    }

    fun updateLanguage(languageCode: String?) {
        updatePreferences { current ->
            val currentLanguage = current.language?.removeRegion()
            val updatedVoices = current.voices.orEmpty().toMutableMap().apply {
                currentLanguage?.let(::remove)
            }
            current.copy(
                language = languageCode?.let(::Language),
                voices = updatedVoices.ifEmpty { null }
            )
        }
    }

    fun updateVoice(voiceId: String?) {
        val editor = initData.navigatorFactory.createPreferencesEditor(preferencesState.value)
        val effectiveLanguage = (editor.language.value ?: editor.language.effectiveValue)
            ?.removeRegion()
            ?: return

        updatePreferences { current ->
            val updatedVoices = current.voices.orEmpty().toMutableMap().apply {
                if (voiceId == null) {
                    remove(effectiveLanguage)
                } else {
                    put(effectiveLanguage, AndroidTtsEngine.Voice.Id(voiceId))
                }
            }
            current.copy(voices = updatedVoices.ifEmpty { null })
        }
    }

    override fun onStopRequested() {
        stop()
    }

    private fun onPlaybackError(error: TtsNavigator.Error) {
        when (error) {
            is TtsNavigator.Error.ContentError -> {
                _errors.tryEmit(ReaderTtsError.ContentFailure(error.cause.message))
                stop()
            }

            is TtsNavigator.Error.EngineError<*> -> {
                when (val engineError = error.cause as AndroidTtsEngine.Error) {
                    is AndroidTtsEngine.Error.LanguageMissingData -> {
                        missingVoiceDataLanguageState.value = engineError.language
                        pause()
                    }

                    else -> {
                        _errors.tryEmit(ReaderTtsError.EngineFailure(engineError.message))
                        stop()
                    }
                }
            }
        }
    }

    private fun updatePreferences(
        updater: (AndroidTtsPreferences) -> AndroidTtsPreferences
    ) {
        viewModelScope.launch {
            initData.preferencesManager.setPreferences(
                updater(preferencesState.value)
            )
        }
    }

    private fun Language.displayName(): String =
        locale.getDisplayLanguage(Locale.getDefault())
            .replaceFirstChar { char ->
                if (char.isLowerCase()) {
                    char.titlecase(Locale.getDefault())
                } else {
                    char.toString()
                }
            }

    private fun bookDefaultLanguageLabel(language: Language?): String =
        language
            ?.displayName()
            ?.takeIf(String::isNotBlank)
            ?.let { languageName ->
                application.getString(
                    com.shubhamghanmode.inkfold.R.string.reader_tts_language_book_default_named,
                    languageName
                )
            }
            ?: application.getString(
                com.shubhamghanmode.inkfold.R.string.reader_tts_language_book_default
            )

    private data class ReaderTtsSnapshot(
        val preferences: AndroidTtsPreferences = AndroidTtsPreferences(),
        val navigator: AndroidTtsNavigator? = null,
        val playback: TtsNavigator.Playback? = null,
        val location: TtsNavigator.Location? = null,
        val isSettingsVisible: Boolean = false
    )
}
