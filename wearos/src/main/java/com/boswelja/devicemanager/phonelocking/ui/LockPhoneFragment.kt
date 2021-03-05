/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.phonelocking.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.boswelja.devicemanager.databinding.FragmentLockPhoneBinding

@Suppress("unused")
class LockPhoneFragment : Fragment() {

    private val viewModel: LockPhoneViewModel by viewModels()

    private lateinit var binding: FragmentLockPhoneBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLockPhoneBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.phoneLockingEnabled.observe(viewLifecycleOwner) {
            if (it) binding.motionLayout.transitionToStart()
            else binding.motionLayout.transitionToEnd()
        }
    }
}
