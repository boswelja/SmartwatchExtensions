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
import com.boswelja.devicemanager.databinding.FragmentDndsyncHelperWarningBinding
import com.boswelja.devicemanager.ui.dndsync.helper.InitialCheckFragmentDirections
import timber.log.Timber

internal class InitialCheckFragment : Fragment() {

    private val viewModel: InitialCheckViewModel by viewModels()

    private lateinit var binding: FragmentDndsyncHelperWarningBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDndsyncHelperWarningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.hasNotiPolicyAccess.observe(viewLifecycleOwner) {
            Timber.i("hasNotiPolicyAccess = $it")
            if (it) {
                findNavController().navigate(InitialCheckFragmentDirections.toResultFragment(true))
            }
        }
        viewModel.hasCorrectSdkInt.observe(viewLifecycleOwner) {
            Timber.i("hasCorrectSdkInt = $it")
            if (it != null) {
                if (!it) {
                    binding.warningText.setText(R.string.dnd_sync_helper_warning_watch_version)
                    setHasWarnings()
                } else {
                    findNavController().navigate(InitialCheckFragmentDirections.toSetupFragment())
                }
                binding.progressBar.hide()
            }
        }
        binding.warningAcknowledged.setOnCheckedChangeListener { _, b -> binding.nextButton.isEnabled = b }
        binding.nextButton.setOnClickListener { findNavController().navigate(R.id.to_setupFragment) }
    }

    private fun setHasWarnings() {
        Timber.i("setHasWarnings() called")
        binding.apply {
            nextButton.visibility = View.VISIBLE
            warningAcknowledged.visibility = View.VISIBLE
            warningIndicator.visibility = View.VISIBLE
            title.text = getString(R.string.dnd_sync_helper_warning_title)
        }
    }
}
