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
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.BuildConfig
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.References.CAPABILITY_PHONE_APP
import com.boswelja.devicemanager.common.setup.References.CHECK_WATCH_REGISTERED_PATH
import com.boswelja.devicemanager.common.setup.References.WATCH_NOT_REGISTERED_PATH
import com.boswelja.devicemanager.common.setup.References.WATCH_REGISTERED_PATH
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_ID_KEY
import com.boswelja.devicemanager.ui.common.LoadingFragment
import com.boswelja.devicemanager.ui.common.NoConnectionFragment
import com.boswelja.devicemanager.ui.main.MainFragment
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

    private val coroutineScope = MainScope()

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var capabilityClient: CapabilityClient
    private lateinit var messageClient: MessageClient

    override fun onMessageReceived(messageEvent: MessageEvent) {
        messageClient.removeListener(this)
        when (messageEvent.path) {
            WATCH_REGISTERED_PATH -> showExtensionsFragment()
            WATCH_NOT_REGISTERED_PATH -> showSetupFragment(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        showLoadingFragment()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        capabilityClient = Wearable.getCapabilityClient(this)
        messageClient = Wearable.getMessageClient(this)

        coroutineScope.launch(Dispatchers.IO) {
            val phoneId = getPhoneId()

            if (!BuildConfig.DEBUG) {
                tryCheckWatchRegistered(phoneId)
            } else {
                showExtensionsFragment()
            }
        }
    }

    /**
     * Shows the [LoadingFragment].
     */
    private fun showLoadingFragment() {
        showFragment(LoadingFragment(), animateChange = false)
    }

    /**
     * Shows the [NoConnectionFragment].
     */
    private fun showNoConnectionFragment() {
        showFragment(NoConnectionFragment())
    }

    /**
     * Shows the [MainFragment].
     */
    private fun showExtensionsFragment() {
        showFragment(MainFragment())
    }

    /**
     * Shows the [SetupFragment].
     * @param phoneHasApp Sets whether the phone already has Wearable Extensions installed.
     */
    private fun showSetupFragment(phoneHasApp: Boolean) {
        SetupFragment().apply {
            setPhoneSetupHelperVisibility(phoneHasApp)
        }.also {
            showFragment(it)
        }
    }

    /**
     * Shows a new fragment.
     * @param fragment The [Fragment] to show.
     * @param animateChange Whether the fragment transaction should be animated.
     */
    private fun showFragment(fragment: Fragment, animateChange: Boolean = true) {
        try {
            supportFragmentManager.beginTransaction().apply {
                if (animateChange) {
                    setCustomAnimations(R.anim.slide_in_right, R.anim.fade_out)
                }
                replace(R.id.content, fragment)
            }.also {
                it.commit()
            }
        } catch (e: IllegalStateException) {
            Log.e("MainActivity",
                    "Tried to commit a FragmentTransaction after onSaveInstanceState")
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
                val isCapable = Tasks.await(capabilityClient
                        .getCapability(CAPABILITY_PHONE_APP, CapabilityClient.FILTER_ALL))
                        .nodes.any { it.id == phoneId }
                if (isCapable) {
                    messageClient.addListener(this@MainActivity)
                    messageClient.sendMessage(phoneId, CHECK_WATCH_REGISTERED_PATH, null)
                            .addOnFailureListener {
                                showNoConnectionFragment()
                            }
                } else {
                    showSetupFragment(false)
                }
            } else {
                showNoConnectionFragment()
            }
        }
    }
}
