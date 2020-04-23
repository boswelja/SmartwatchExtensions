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
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.R
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SetupFragment : Fragment() {

    private val coroutineScope = MainScope()

    private var phoneHasApp: Boolean = false

    private lateinit var nodeClient: NodeClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nodeClient = Wearable.getNodeClient(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<TextView>(R.id.setup_device_name_text).apply {
            coroutineScope.launch(Dispatchers.IO) {
                val localNode = Tasks.await(nodeClient.localNode)
                withContext(Dispatchers.Main) {
                    text = localNode.displayName
                }
            }
        }
        setPhoneSetupHelperVisibility(phoneHasApp)
    }

    fun setPhoneSetupHelperVisibility(phoneHasApp: Boolean) {
        this.phoneHasApp = phoneHasApp
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
