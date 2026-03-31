package com.shubhamghanmode.inkfold.feature.reader

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.commit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.shubhamghanmode.inkfold.InkFoldApplication
import com.shubhamghanmode.inkfold.R
import com.shubhamghanmode.inkfold.feature.reader.preferences.ReaderThemeOption
import com.shubhamghanmode.inkfold.feature.reader.tts.ReaderTtsError
import com.shubhamghanmode.inkfold.ui.theme.AppThemeSettings
import com.shubhamghanmode.inkfold.ui.theme.InkFoldTheme
import kotlinx.coroutines.launch
import org.readium.navigator.media.tts.android.AndroidTtsEngine
import org.readium.r2.shared.ExperimentalReadiumApi

@OptIn(ExperimentalReadiumApi::class)
class ReaderActivity : AppCompatActivity() {
    private val appContainer by lazy { (application as InkFoldApplication).appContainer }
    private val viewModel: ReaderViewModel by viewModels {
        ReaderViewModel.factory(ReaderActivityContract.parseBookId(intent))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reader)

        findViewById<ComposeView>(R.id.readerOverlayComposeView).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
                val appThemeSettings by appContainer.appThemePreferencesRepository.settings.collectAsStateWithLifecycle(
                    initialValue = AppThemeSettings()
                )

                InkFoldTheme(
                    themePreset = appThemeSettings.themePreset,
                    darkStatusBarIcons = uiState.appearance.selectedTheme != ReaderThemeOption.DARK
                ) {
                    ReaderOverlay(
                        uiState = uiState,
                        onNavigateBack = {
                            onBackPressedDispatcher.onBackPressed()
                        },
                        onOpenSettings = viewModel::openSettings,
                        onCloseSettings = viewModel::closeSettings,
                        onThemeSelected = viewModel::selectTheme,
                        onScrollModeSelected = viewModel::selectScrollMode,
                        onFontSizeChange = viewModel::updateFontSize,
                        onIncreaseFontSize = viewModel::increaseFontSize,
                        onDecreaseFontSize = viewModel::decreaseFontSize,
                        onTypefaceSelected = viewModel::selectTypeface,
                        onImageFilterSelected = viewModel::selectImageFilter,
                        onPageMarginPresetSelected = viewModel::selectPageMarginPreset,
                        onIncreasePageMargins = viewModel::increasePageMargins,
                        onDecreasePageMargins = viewModel::decreasePageMargins,
                        onJumpToProgression = viewModel::jumpToProgression,
                        onSliderChangeFinished = viewModel::flushAppearanceChanges,
                        onResetAppearance = viewModel::resetAppearance,
                        onOpenOutline = viewModel::openOutline,
                        onCloseOutline = viewModel::closeOutline,
                        onOutlineSectionSelected = viewModel::selectOutlineSection,
                        onOutlineItemSelected = viewModel::navigateToOutlineItem,
                        onRequestReadAloud = viewModel::requestStartReadAloud,
                        onToggleReadAloudPlayback = viewModel::toggleReadAloudPlayback,
                        onReadPreviousUtterance = viewModel::readPreviousUtterance,
                        onReadNextUtterance = viewModel::readNextUtterance,
                        onStopReadAloud = viewModel::stopReadAloud,
                        onOpenReadAloudSettings = viewModel::openReadAloudSettings,
                        onCloseReadAloudSettings = viewModel::closeReadAloudSettings,
                        onReadAloudSpeedChange = viewModel::updateReadAloudSpeed,
                        onReadAloudPitchChange = viewModel::updateReadAloudPitch,
                        onReadAloudLanguageSelected = viewModel::selectReadAloudLanguage,
                        onReadAloudVoiceSelected = viewModel::selectReadAloudVoice,
                        onDismissMissingVoiceDataPrompt = viewModel::dismissMissingVoiceDataPrompt,
                        onInstallMissingVoiceData = {
                            AndroidTtsEngine.requestInstallVoice(this@ReaderActivity)
                            viewModel.dismissMissingVoiceDataPrompt()
                        }
                    )
                }
            }
        }

        if (supportFragmentManager.findFragmentByTag(READER_FRAGMENT_TAG) == null) {
            supportFragmentManager.commit {
                replace(R.id.readerFragmentContainer, ReaderFragment::class.java, Bundle(), READER_FRAGMENT_TAG)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    state.errorMessage?.let { message ->
                        Toast.makeText(this@ReaderActivity, message, Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.ttsErrors.collect { error ->
                    Toast.makeText(
                        this@ReaderActivity,
                        messageForTtsError(error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onPause() {
        viewModel.flushAppearanceChanges()
        viewModel.pauseReadAloudForBackground()
        super.onPause()
    }

    companion object {
        private const val READER_FRAGMENT_TAG = "reader-fragment"
    }

    private fun messageForTtsError(error: ReaderTtsError): String =
        when (error) {
            ReaderTtsError.InitializationFailed -> getString(R.string.reader_tts_error_initialization)
            ReaderTtsError.UnsupportedPublication -> getString(R.string.reader_tts_error_unsupported)
            is ReaderTtsError.ContentFailure -> getString(R.string.reader_tts_error_content)
            is ReaderTtsError.EngineFailure -> getString(R.string.reader_tts_error_engine)
        }
}
