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
import com.boswelja.devicemanager.common.appmanager.AppPackageInfoList
import com.boswelja.devicemanager.common.appmanager.References
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity
import com.boswelja.devicemanager.ui.base.LoadingFragment
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable

class AppManagerActivity : BaseToolbarActivity() {

    private lateinit var messageClient: MessageClient

    private var appManagerFragment: AppManagerFragment? = null

    private var isServiceRunning: Boolean = false
    var canStopService: Boolean = true
    var watchId: String? = null

    private val messageListener = MessageClient.OnMessageReceivedListener {
        when (it.path) {
            References.GET_ALL_PACKAGES -> {
                val allApps = AppPackageInfoList.fromByteArray(it.data)
                ensureAppManagerVisible()
                appManagerFragment!!.setAllApps(allApps)
            }
        }
    }

    override fun getContentViewId(): Int = R.layout.activity_app_manager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        watchId = intent?.getStringExtra(EXTRA_WATCH_ID)
        if (watchId.isNullOrEmpty()) {
            notifyWatchNotFound()
        }

        supportActionBar?.apply {
            setTitle(R.string.app_manager_activity_title)
            subtitle = getString(R.string.app_manager_activity_subtitle, intent.getStringExtra(EXTRA_WATCH_NAME))
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(true)
        }

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_holder, LoadingFragment())
                .commit()

        messageClient = Wearable.getMessageClient(this)
    }

    override fun onResume() {
        super.onResume()
        messageClient.addListener(messageListener)
        startAppManagerService()
    }

    override fun onPause() {
        super.onPause()
        messageClient.removeListener(messageListener)
        if (canStopService) stopAppManagerService()
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

    private fun startAppManagerService() {
        if (!isServiceRunning) {
            isServiceRunning = true
            messageClient.sendMessage(watchId!!, References.START_SERVICE, null)
        }
    }

    private fun stopAppManagerService() {
        isServiceRunning = false
        messageClient.sendMessage(watchId!!, References.STOP_SERVICE, null)
    }

    private fun notifyWatchNotFound() {
        Toast.makeText(this, getString(R.string.app_manager_unable_to_connect), Toast.LENGTH_LONG).show()
        finish()
    }

    companion object {
        const val EXTRA_WATCH_ID = "extra_watch_id"
        const val EXTRA_WATCH_NAME = "extra_watch_name"
    }
}
