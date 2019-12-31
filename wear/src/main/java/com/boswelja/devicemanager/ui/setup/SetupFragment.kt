/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.setup

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.setup.References.WATCH_REGISTERED_PATH
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_ID_KEY
import com.boswelja.devicemanager.ui.MainActivity
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SetupFragment :
        Fragment(),
        MessageClient.OnMessageReceivedListener {

    private val coroutineScope = MainScope()

    private lateinit var messageClient: MessageClient
    private lateinit var nodeClient: NodeClient
    private lateinit var sharedPreferences: SharedPreferences

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val senderId = messageEvent.sourceNodeId
        if (!senderId.isNullOrEmpty()) {
            when (messageEvent.path) {
                WATCH_REGISTERED_PATH -> {
                    sharedPreferences.edit {
                        putString(PHONE_ID_KEY, messageEvent.sourceNodeId)
                    }

                    (activity as MainActivity?)?.setupFinished()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        messageClient = Wearable.getMessageClient(context!!)
        nodeClient = Wearable.getNodeClient(context!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<TextView>(R.id.setup_device_name_text).apply {
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    val localNode = Tasks.await(nodeClient.localNode)
                    withContext(Dispatchers.Main) {
                        text = localNode.displayName
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        messageClient.addListener(this)
    }

    override fun onPause() {
        super.onPause()
        messageClient.removeListener(this)
    }

    fun setPhoneSetupHelperVisibility(phoneHasApp: Boolean) {
        view?.findViewById<View>(R.id.phone_setup_helper_view)!!.apply {
            visibility = if (phoneHasApp) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }
}
