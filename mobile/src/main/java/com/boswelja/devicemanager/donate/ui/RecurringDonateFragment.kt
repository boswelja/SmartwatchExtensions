package com.boswelja.devicemanager.donate.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.boswelja.devicemanager.common.ui.fragment.ListFragment

class RecurringDonateFragment : ListFragment() {

    private val viewModel: DonateViewModel by activityViewModels()
    private val adapter by lazy {
        DonateAdapter {
            viewModel.launchBillingFlow(requireActivity(), it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = adapter
        viewModel.recurringDonations.observe(viewLifecycleOwner) {
            isLoading = it.isEmpty()
            adapter.submitList(it)
        }
    }
}
