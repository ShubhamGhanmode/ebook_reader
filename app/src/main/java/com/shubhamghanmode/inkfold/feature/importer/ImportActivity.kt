package com.shubhamghanmode.inkfold.feature.importer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.shubhamghanmode.inkfold.InkFoldApplication
import com.shubhamghanmode.inkfold.MainActivity
import com.shubhamghanmode.inkfold.feature.reader.ReaderActivityContract
import kotlinx.coroutines.launch

class ImportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            handleIncomingIntent(intent)
            finish()
        }
    }

    private suspend fun handleIncomingIntent(intent: Intent) {
        val uri = extractUri(intent)
            ?: run {
                openHome("InkFold did not receive a readable EPUB file.")
                return
            }

        val appContainer = (application as InkFoldApplication).appContainer

        appContainer.libraryRepository.importFromUri(uri)
            .onFailure { error ->
                openHome(error.message ?: "InkFold could not import that EPUB.")
                return
            }
            .onSuccess { bookId ->
                appContainer.readerRepository.prepare(bookId)
                    .onFailure { error ->
                        openHome(error.message ?: "InkFold could not open that EPUB.")
                    }
                    .onSuccess {
                        startActivity(ReaderActivityContract.createIntent(this, bookId))
                    }
            }
    }

    private fun openHome(message: String) {
        startActivity(
            MainActivity.createIntent(
                context = this,
                transientMessage = message
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        )
    }

    private fun extractUri(intent: Intent): Uri? =
        when (intent.action) {
            Intent.ACTION_SEND -> intent.readParcelableUriExtra(Intent.EXTRA_STREAM)
            Intent.ACTION_VIEW -> intent.data
            else -> null
        }

    private fun Intent.readParcelableUriExtra(name: String): Uri? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(name, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(name) as? Uri
        }
}
