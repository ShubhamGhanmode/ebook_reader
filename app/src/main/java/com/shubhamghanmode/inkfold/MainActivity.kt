package com.shubhamghanmode.inkfold

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.shubhamghanmode.inkfold.feature.home.HomeScreen
import com.shubhamghanmode.inkfold.feature.home.HomeViewModel
import com.shubhamghanmode.inkfold.feature.reader.ReaderActivityContract
import com.shubhamghanmode.inkfold.ui.theme.InkFoldTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModel.factory((application as InkFoldApplication).appContainer)
    }

    private val importDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let(homeViewModel::importFromUri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleLaunchIntent(intent)

        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                homeViewModel.openBookRequests.collect { bookId ->
                    startActivity(ReaderActivityContract.createIntent(this@MainActivity, bookId))
                }
            }
        }

        setContent {
            val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

            InkFoldTheme {
                HomeScreen(
                    uiState = uiState,
                    onImportClick = {
                        importDocumentLauncher.launch(arrayOf("*/*"))
                    },
                    onOpenBook = homeViewModel::openBook,
                    onDeleteBook = homeViewModel::deleteBook,
                    onTransientMessageShown = homeViewModel::consumeTransientMessage
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLaunchIntent(intent)
    }

    private fun handleLaunchIntent(intent: Intent?) {
        val transientMessage = intent?.getStringExtra(EXTRA_TRANSIENT_MESSAGE)
        if (!transientMessage.isNullOrBlank()) {
            homeViewModel.showTransientMessage(transientMessage)
            intent.removeExtra(EXTRA_TRANSIENT_MESSAGE)
        }
    }

    companion object {
        const val EXTRA_TRANSIENT_MESSAGE = "com.shubhamghanmode.inkfold.TRANSIENT_MESSAGE"

        fun createIntent(
            context: Context,
            transientMessage: String? = null
        ): Intent =
            Intent(context, MainActivity::class.java).apply {
                transientMessage?.let {
                    putExtra(EXTRA_TRANSIENT_MESSAGE, it)
                }
            }
    }
}
