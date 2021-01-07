package com.boswelja.devicemanager.watchmanager.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.adapter.WatchAdapter
import com.boswelja.devicemanager.databinding.FragmentWatchRegisterResultsBinding
import com.boswelja.devicemanager.watchmanager.item.Watch

/**
 * Fragment to show the results of [RegisterWatchViewModel.registerAvailableWatches] to the user.
 */
class WatchRegisterResultsFragment : Fragment() {

    private val viewModel: RegisterWatchViewModel by viewModels({ requireParentFragment() })
    private val adapter: WatchAdapter by lazy { WatchAdapter(null) }

    private lateinit var binding: FragmentWatchRegisterResultsBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentWatchRegisterResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.isWorking.observe(viewLifecycleOwner) {
            // If ViewModel is not doing work, set up views
            if (!it) {
                if (viewModel.registeredWatches.isNotEmpty()) {
                    showWatchesRegistered(viewModel.registeredWatches)
                } else {
                    showNoChanges()
                }
            }
        }

        // Set up common elements
        binding.finishButton.setOnClickListener { requireActivity().finish() }
        binding.tryAgainButton.setOnClickListener { viewModel.registerAvailableWatches() }
        binding.registeredWatchesRecyclerview.adapter = adapter
    }

    /**
     * Set up views to show the watches that were registered.
     */
    private fun showWatchesRegistered(watches: List<Watch>) {
        adapter.submitList(watches)
        binding.tryAgainButton.visibility = View.GONE
        binding.statusTitle.text =
                resources.getQuantityText(R.plurals.register_watch_success_title, watches.count())
        binding.statusText.text = getString(R.string.register_watch_success_info)
        binding.statusIndicator.setImageResource(R.drawable.wizard_ic_success)
    }

    /**
     * Set up views to notify the user that no watches were found.
     */
    private fun showNoChanges() {
        binding.tryAgainButton.visibility = View.VISIBLE
        binding.statusTitle.text = getString(R.string.register_watch_no_watches_title)
        binding.statusText.text = getString(R.string.register_watch_no_watches_info)
        binding.statusIndicator.setImageResource(R.drawable.wizard_ic_warning)

    }
}