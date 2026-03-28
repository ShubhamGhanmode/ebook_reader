package com.shubhamghanmode.inkfold.feature.reader

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.commit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.shubhamghanmode.inkfold.R
import com.shubhamghanmode.inkfold.ui.theme.InkFoldTheme
import kotlinx.coroutines.launch

class ReaderActivity : AppCompatActivity() {
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

                InkFoldTheme {
                    ReaderOverlay(
                        uiState = uiState,
                        onNavigateBack = {
                            onBackPressedDispatcher.onBackPressed()
                        },
                        onOpenSettings = viewModel::openSettings,
                        onCloseSettings = viewModel::closeSettings,
                        onThemeSelected = viewModel::selectTheme,
                        onFontSizeChange = viewModel::updateFontSize,
                        onPageMarginsChange = viewModel::updatePageMargins,
                        onSliderChangeFinished = viewModel::flushAppearanceChanges,
                        onResetAppearance = viewModel::resetAppearance
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
    }

    override fun onPause() {
        viewModel.flushAppearanceChanges()
        super.onPause()
    }

    companion object {
        private const val READER_FRAGMENT_TAG = "reader-fragment"
    }
}
