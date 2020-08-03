/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.References.CAPABILITY_PHONE_APP
import com.boswelja.devicemanager.common.setup.References.CHECK_WATCH_REGISTERED_PATH
import com.boswelja.devicemanager.common.setup.References.WATCH_NOT_REGISTERED_PATH
import com.boswelja.devicemanager.common.setup.References.WATCH_REGISTERED_PATH
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_ID_KEY
import com.boswelja.devicemanager.ui.common.NoConnectionFragment
import com.boswelja.devicemanager.ui.setup.SetupFragment
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity :
    AppCompatActivity(),
    MessageClient.OnMessageReceivedListener {

    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private val coroutineScope = MainScope()

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var capabilityClient: CapabilityClient
    private lateinit var messageClient: MessageClient

    override fun onMessageReceived(messageEvent: MessageEvent) {
        messageClient.removeListener(this)
        when (messageEvent.path) {
            WATCH_REGISTERED_PATH -> navController.navigate(R.id.to_mainFragment)
            WATCH_NOT_REGISTERED_PATH -> navController.navigate(R.id.to_setupFragment)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        capabilityClient = Wearable.getCapabilityClient(this)
        messageClient = Wearable.getMessageClient(this)

        coroutineScope.launch(Dispatchers.IO) {
            val phoneId = getPhoneId()
            tryCheckWatchRegistered(phoneId)
        }
    }

    /**
     * Get the connected phone ID.
     * @return The connected phone ID, or null if no phone is found.
     */
    private suspend fun getPhoneId(): String? {
        return withContext(Dispatchers.IO) {
            val storedId = sharedPreferences.getString(PHONE_ID_KEY, "")
            if (storedId.isNullOrEmpty()) {
                // No known connected phone, attempting to get a new ID
                val node = Tasks.await(Wearable.getNodeClient(this@MainActivity).connectedNodes)
                    .firstOrNull()
                sharedPreferences.edit {
                    putString(PHONE_ID_KEY, node?.id)
                }
                return@withContext node?.id
            } else {
                return@withContext storedId
            }
        }
    }

    /**
     * Check whether this watch is registered with a phone with a given ID.
     * If there's a problem connecting to the phone, a [NoConnectionFragment] is shown.
     * If the phone isn't capable (i.e. doesn't have Wearable Extensions installed),
     * a [SetupFragment] is shown.
     * @param phoneId The ID of the phone to check with.
     */
    private suspend fun tryCheckWatchRegistered(phoneId: String?) {
        withContext(Dispatchers.IO) {
            if (!phoneId.isNullOrEmpty()) {
                val isCapable = Tasks.await(
                    capabilityClient
                        .getCapability(CAPABILITY_PHONE_APP, CapabilityClient.FILTER_ALL)
                )
                    .nodes.any { it.id == phoneId }
                if (isCapable) {
                    messageClient.addListener(this@MainActivity)
                    messageClient.sendMessage(phoneId, CHECK_WATCH_REGISTERED_PATH, null)
                        .addOnFailureListener {
                            // TODO Fix regression here
                        }
                } else {
                    navController.navigate(R.id.to_setupFragment)
                }
            } else {
                // TODO Fix regression here
            }
        }
    }
}
