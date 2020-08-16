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
import androidx.navigation.fragment.navArgs
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.databinding.FragmentDndsyncHelperResultBinding
import com.boswelja.devicemanager.ui.dndsync.helper.ResultFragmentArgs

internal class ResultFragment : Fragment() {

    private val args: ResultFragmentArgs by navArgs()
    private val viewModel: ResultViewModel by viewModels()

    private lateinit var binding: FragmentDndsyncHelperResultBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDndsyncHelperResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.setSyncToWatch(args.success)
        binding.apply {
            if (args.success) {
                resultIndicator.setImageResource(R.drawable.wizard_ic_success)
                resultTitle.setText(R.string.dnd_sync_helper_success_title)
                resultStatus.setText(R.string.dnd_sync_helper_success_message)
            } else {
                resultIndicator.setImageResource(R.drawable.wizard_ic_fail)
                resultTitle.setText(R.string.dnd_sync_helper_failed_title)
                resultStatus.setText(R.string.dnd_sync_helper_failed_message)
            }
            finishButton.setOnClickListener {
                activity?.finish()
            }
        }
    }
}
