/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.boswelja.devicemanager.common.LifecycleAwareTimer
import com.boswelja.devicemanager.common.ui.adapter.WatchAdapter
import com.boswelja.devicemanager.databinding.FragmentRegisterWatchBinding

class RegisterWatchFragment : Fragment() {

    private val viewModel: RegisterWatchViewModel by viewModels()
    private val adapter: WatchAdapter by lazy { WatchAdapter(null) }
    private val availableWatchUpdateTimer = LifecycleAwareTimer(5) {
        viewModel.refreshData()
    }

    private lateinit var binding: FragmentRegisterWatchBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterWatchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeAvailableWatches()

        binding.registeredWatchesRecyclerview.adapter = adapter

        viewModel.registeredWatches.observe(viewLifecycleOwner) {
            binding.finishButton.isEnabled = it.isNotEmpty()
            adapter.submitList(it)
        }

        lifecycle.addObserver(availableWatchUpdateTimer)

        binding.finishButton.setOnClickListener {
            requireActivity().finish()
        }
    }

    private fun observeAvailableWatches() {
        viewModel.availableWatches.observe(viewLifecycleOwner) {
            it.forEach { watch ->
                viewModel.registerWatch(watch)
            }
        }
    }
}
