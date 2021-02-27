/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.databinding.CommonFragmentLoadingBinding
import timber.log.Timber

class LoadingFragment : Fragment() {

    private lateinit var binding: CommonFragmentLoadingBinding

    /**
     * The progress the indicator should display, or -1 for indeterminate.
     */
    val progress = MutableLiveData(-1)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CommonFragmentLoadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.loadingText.text = getString(R.string.dnd_sync_helper_loading_text)
        progress.observe(viewLifecycleOwner) {
            if (it > -1) {
                binding.progressBar.progress = it
            }
            binding.progressBar.isIndeterminate = it > -1
        }
        Timber.i("Successfully created")
    }
}
