package com.shubhamghanmode.inkfold.feature.reader

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commitNow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.shubhamghanmode.inkfold.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.input.InputListener
import org.readium.r2.navigator.input.TapEvent
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.data.ReadError

@OptIn(ExperimentalReadiumApi::class)
class ReaderFragment : Fragment(), EpubNavigatorFragment.Listener {
    private val viewModel: ReaderViewModel by activityViewModels(
        extrasProducer = { requireActivity().defaultViewModelCreationExtras }
    ) {
        ReaderViewModel.factory(ReaderActivityContract.parseBookId(requireActivity().intent))
    }
    private var navigator: EpubNavigatorFragment? = null
    private var locatorJob: Job? = null
    private var preferencesJob: Job? = null
    private var chromeTapListener: InputListener? = null
    private var attachedBookId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        childFragmentManager.fragmentFactory =
            viewModel.uiState.value.session
                ?.navigatorFactory
                ?.createFragmentFactory(
                    initialLocator = viewModel.uiState.value.session?.initialLocator,
                    initialPreferences = viewModel.uiState.value.session?.initialPreferences ?: EpubPreferences(),
                    listener = this
                )
                ?: EpubNavigatorFragment.createDummyFactory()

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        inflater.inflate(R.layout.fragment_reader, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    state.session?.let(::attachNavigator)
                }
            }
        }
    }

    private fun attachNavigator(session: ReaderSession) {
        if (attachedBookId == session.bookId && navigator != null) {
            return
        }

        clearNavigatorBindings()

        childFragmentManager.fragmentFactory = session.navigatorFactory.createFragmentFactory(
            initialLocator = session.initialLocator,
            initialPreferences = session.initialPreferences,
            listener = this
        )

        childFragmentManager.commitNow {
            replace(
                R.id.readerNavigatorContainer,
                EpubNavigatorFragment::class.java,
                Bundle(),
                NAVIGATOR_FRAGMENT_TAG
            )
        }

        navigator = childFragmentManager.findFragmentByTag(NAVIGATOR_FRAGMENT_TAG) as? EpubNavigatorFragment
        attachedBookId = session.bookId

        navigator?.let(::startProgressTracking)
        navigator?.let(::bindPreferences)
        navigator?.let(::bindChromeTapHandling)
    }

    private fun startProgressTracking(navigatorFragment: EpubNavigatorFragment) {
        locatorJob = viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                navigatorFragment.currentLocator.collect { locator ->
                    viewModel.saveProgression(locator)
                }
            }
        }
    }

    override fun onStop() {
        navigator?.currentLocator?.value?.let(viewModel::saveProgression)
        super.onStop()
    }

    override fun onDestroyView() {
        clearNavigatorBindings()
        super.onDestroyView()
    }

    override fun onExternalLinkActivated(url: AbsoluteUrl) {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, url.toString().toUri()))
        }
    }

    override fun onResourceLoadFailed(href: Url, error: ReadError) {
        Toast.makeText(
            requireContext(),
            getString(R.string.reader_resource_error),
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        private const val NAVIGATOR_FRAGMENT_TAG = "epub-navigator"
    }

    private fun bindPreferences(navigatorFragment: EpubNavigatorFragment) {
        preferencesJob = viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigatorPreferences.collect { preferences ->
                    navigatorFragment.submitPreferences(preferences)
                }
            }
        }
    }

    private fun bindChromeTapHandling(navigatorFragment: EpubNavigatorFragment) {
        val centerTapListener = object : InputListener {
            override fun onTap(event: TapEvent): Boolean {
                val width = navigatorFragment.publicationView.width.toFloat()
                val height = navigatorFragment.publicationView.height.toFloat()

                if (width <= 0f || height <= 0f) {
                    return false
                }

                val isCenterTap = event.point.x in (width * 0.24f)..(width * 0.76f) &&
                    event.point.y in (height * 0.18f)..(height * 0.82f)

                if (!isCenterTap) {
                    return false
                }

                viewModel.onReaderSurfaceTapped()
                return true
            }
        }

        navigatorFragment.addInputListener(centerTapListener)
        chromeTapListener = centerTapListener
    }

    private fun clearNavigatorBindings() {
        locatorJob?.cancel()
        locatorJob = null
        preferencesJob?.cancel()
        preferencesJob = null
        chromeTapListener?.let { inputListener ->
            navigator?.removeInputListener(inputListener)
        }
        chromeTapListener = null
        navigator = null
    }
}
