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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.databinding.CommonFragmentLoadingBinding
import timber.log.Timber

open class LoadingFragment : Fragment() {

    private lateinit var binding: CommonFragmentLoadingBinding

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
        Timber.i("Successfully created")
    }

    private fun setProgressIndeterninate(isIndeterminate: Boolean) {
        if (binding.progressBar.isIndeterminate != isIndeterminate) {
            binding.progressBar.isVisible = false
            binding.progressBar.isIndeterminate = isIndeterminate
            binding.progressBar.isVisible = true
        }
    }

    /**
     * Set the progress displayed to the user. Use -1 to set indeterminate.
     * @param progress The progress to display.
     */
    fun setProgress(progress: Int) {
        if (progress > -1) {
            binding.progressBar.progress = progress
            setProgressIndeterninate(false)
        } else {
            setProgressIndeterninate(true)
        }
    }
}
