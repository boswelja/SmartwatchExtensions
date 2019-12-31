/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.References.CAPABILITY_PHONE_APP
import com.boswelja.devicemanager.common.setup.References.CHECK_WATCH_REGISTERED_PATH
import com.boswelja.devicemanager.common.setup.References.WATCH_NOT_REGISTERED_PATH
import com.boswelja.devicemanager.common.setup.References.WATCH_REGISTERED_PATH
import com.boswelja.devicemanager.ui.common.LoadingFragment
import com.boswelja.devicemanager.ui.common.NoConnectionFragment
import com.boswelja.devicemanager.ui.main.ExtensionsFragment
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

    private val loadingFragment: LoadingFragment = LoadingFragment()
    private var extensionsFragment: ExtensionsFragment? = null
    private var setupFragment: SetupFragment? = null
    private var noConnectionFragment: NoConnectionFragment? = null

    private var shouldAnimateFragmentChanges: Boolean = false

    private lateinit var capabilityClient: CapabilityClient
    private lateinit var messageClient: MessageClient

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            WATCH_REGISTERED_PATH -> showExtensionsFragment()
            WATCH_NOT_REGISTERED_PATH -> {
                showSetupFragment(true)
            }
        }
        messageClient.removeListener(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        showLoadingFragment()

        capabilityClient = Wearable.getCapabilityClient(this)
        messageClient = Wearable.getMessageClient(this)
        messageClient.addListener(this)

        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val phoneNode = Tasks.await(Wearable.getNodeClient(this@MainActivity).connectedNodes).firstOrNull()
                val isCapable = Tasks.await(capabilityClient.getCapability(CAPABILITY_PHONE_APP, CapabilityClient.FILTER_ALL)).nodes.any { it.id == phoneNode?.id }
                if (phoneNode != null && isCapable) {
                    messageClient.sendMessage(phoneNode.id, CHECK_WATCH_REGISTERED_PATH, null)
                            .addOnFailureListener {
                                showNoConnectionFragment()
                            }
                } else if (phoneNode != null && !isCapable) {
                    showSetupFragment(false)
                } else {
                    showNoConnectionFragment()
                }
            }
        }
    }

    private fun showLoadingFragment() {
        showFragment(loadingFragment)
    }

    private fun showNoConnectionFragment() {
        if (noConnectionFragment == null) {
            noConnectionFragment = NoConnectionFragment()
        }
        showFragment(noConnectionFragment!!)
    }

    private fun showExtensionsFragment() {
        if (extensionsFragment == null) {
            extensionsFragment = ExtensionsFragment()
        }
        showFragment(extensionsFragment!!)
    }

    private fun showSetupFragment(phoneHasApp: Boolean) {
        if (setupFragment == null) {
            setupFragment = SetupFragment()
        }
        setupFragment!!.phoneHasApp = phoneHasApp
        showFragment(setupFragment!!)
    }

    private fun showFragment(fragment: Fragment) {
        try {
            supportFragmentManager.beginTransaction().apply {
                if (shouldAnimateFragmentChanges) {
                    setCustomAnimations(R.anim.slide_in_right, R.anim.fade_out)
                } else {
                    shouldAnimateFragmentChanges = true
                }
                replace(R.id.content, fragment)
            }.also {
                it.commit()
            }
        } catch (e: IllegalStateException) {
            Log.e("MainActivity", "Tried to commit a FragmentTransaction after onSaveInstanceState")
        }
    }

    fun setupFinished() {
        showExtensionsFragment()
    }
}
