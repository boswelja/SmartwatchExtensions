/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.appmanager

import android.os.Bundle
import android.widget.Toast
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.appmanager.AppManagerReferences
import com.boswelja.devicemanager.common.appmanager.AppPackageInfoList
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity
import com.boswelja.devicemanager.ui.base.LoadingFragment
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable

class AppManagerActivity : BaseToolbarActivity() {

    override fun getContentViewId(): Int = R.layout.activity_app_manager

    private lateinit var messageClient: MessageClient

    private var appManagerFragment: AppManagerFragment? = null

    private val messageListener = MessageClient.OnMessageReceivedListener {
        when (it.path) {
            AppManagerReferences.GET_ALL_PACKAGES -> {
                val allApps = AppPackageInfoList.fromByteArray(it.data)
                ensureAppManagerVisible()
                appManagerFragment!!.setAllApps(allApps)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_holder, LoadingFragment())
                .commit()

        messageClient = Wearable.getMessageClient(this)

        startAppManagerService()
    }

    override fun onResume() {
        super.onResume()
        messageClient.addListener(messageListener)
    }

    override fun onPause() {
        super.onPause()
        messageClient.removeListener(messageListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAppManagerService()
    }

    private fun ensureAppManagerVisible() {
        if (appManagerFragment == null) {
            appManagerFragment = AppManagerFragment()
            try {
                supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        .replace(R.id.fragment_holder, appManagerFragment!!)
                        .commit()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    fun startAppManagerService() {
        if (!connectedWatchId.isNullOrEmpty()) {
            messageClient.sendMessage(connectedWatchId!!, AppManagerReferences.START_SERVICE, null)
        } else {
            Toast.makeText(this, getString(R.string.app_manager_unable_to_connect), Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun stopAppManagerService() {
        if (!connectedWatchId.isNullOrEmpty()) {
            messageClient.sendMessage(connectedWatchId!!, AppManagerReferences.STOP_SERVICE, null)
        }
    }
}
