/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.databinding.FragmentSetupBinding
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable

class SetupFragment : Fragment() {

    private lateinit var nodeClient: NodeClient
    private lateinit var binding: FragmentSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nodeClient = Wearable.getNodeClient(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSetupBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.setupDeviceNameText.apply {
            nodeClient.localNode
                    .addOnCompleteListener {
                        text = it.result?.displayName ?: getString(R.string.error)
                    }
        }
    }

    fun setPhoneSetupHelperVisibility(phoneHasApp: Boolean) {
        if (view != null) {
            view?.findViewById<View>(R.id.phone_setup_helper_view)!!.apply {
                visibility = if (phoneHasApp) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }
        }
    }
}
