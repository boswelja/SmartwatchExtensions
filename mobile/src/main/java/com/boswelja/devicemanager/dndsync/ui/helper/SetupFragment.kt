/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.dndsync.ui.helper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.recyclerview.adapter.StringAdapter
import com.boswelja.devicemanager.databinding.FragmentDndsyncHelperSetupBinding

internal class SetupFragment : Fragment() {

    private val viewModel: SetupViewModel by viewModels()

    private lateinit var binding: FragmentDndsyncHelperSetupBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDndsyncHelperSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.hasNotiPolicyAccess.observe(viewLifecycleOwner) {
            if (it != null) {
                val action = SetupFragmentDirections.toResultFragment(it)
                findNavController().navigate(action)
                viewModel.permissionRequestHandled()
            }
        }

        binding.stepRecyclerview.adapter =
            StringAdapter(resources.getStringArray(R.array.interrupt_filter_sync_to_watch_steps))
        binding.nextButton.setOnClickListener {
            setLoading(true)
            viewModel.requestCheckPermission()
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.apply {
            if (isLoading) progressBar.show() else progressBar.hide()
            nextButton.isEnabled = !isLoading
        }
    }
}
