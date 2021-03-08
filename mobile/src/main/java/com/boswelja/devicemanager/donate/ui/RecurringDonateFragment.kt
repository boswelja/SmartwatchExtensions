package com.boswelja.devicemanager.donate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel

class RecurringDonateFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val viewModel: DonateViewModel = viewModel()
                val isLoading = viewModel.clientConnected.observeAsState()
                val donateOptions = viewModel.recurringDonations.observeAsState()
                Column(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                ) {
                    isLoading.value?.let {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                    donateOptions.value?.let { donateOptions ->
                        DonateList(
                            donateOptions = donateOptions,
                            onClick = { viewModel.launchBillingFlow(requireActivity(), it) }
                        )
                    }
                }
            }
        }
    }
}
