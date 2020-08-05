/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return View(requireContext()) // Empty view so onViewCreated gets called
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.isRegistered.observe(viewLifecycleOwner) {
            it?.let { isRegistered ->
                if (isRegistered) {
                    findNavController().navigate(MainFragmentDirections.toExtensionsFragment())
                } else {
                    findNavController().navigate(MainFragmentDirections.toSetupFragment())
                }
            }
        }
    }
}
