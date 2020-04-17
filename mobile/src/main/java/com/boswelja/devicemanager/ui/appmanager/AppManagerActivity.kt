/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
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
import timber.log.Timber

class AppManagerActivity : BaseToolbarActivity() {

    private lateinit var messageClient: MessageClient

    private var appManagerFragment: AppManagerFragment? = null
    private var isServiceRunning: Boolean = false

    var canStopAppManagerService: Boolean = true
    var watchId: String? = null

    private val messageListener = MessageClient.OnMessageReceivedListener {
        Timber.i("Received a message")
        when (it.path) {
            References.GET_ALL_PACKAGES -> {
                Timber.i("Updating app list")
                val allApps = AppPackageInfoList.fromByteArray(it.data)
                ensureAppManagerVisible()
                appManagerFragment!!.setAllApps(allApps)
            }
        }
    }

    override fun getContentViewId(): Int = R.layout.activity_app_manager

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.i("onCreate() called")
        super.onCreate(savedInstanceState)

        watchId = intent?.getStringExtra(EXTRA_WATCH_ID)
        if (watchId.isNullOrEmpty()) {
            notifyWatchNotFound()
            return
        }

        supportActionBar?.apply {
            subtitle = getString(
                    R.string.app_manager_activity_subtitle,
                    intent.getStringExtra(EXTRA_WATCH_NAME))
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(true)
        }

        showLoadingFragment()

        messageClient = Wearable.getMessageClient(this)
    }

    override fun onStart() {
        Timber.i("onStart() called")
        super.onStart()
        messageClient.addListener(messageListener)
        startAppManagerService()
    }

    override fun onStop() {
        Timber.i("onStop() called")
        super.onStop()
        messageClient.removeListener(messageListener)
        if (canStopAppManagerService) stopAppManagerService()
    }

    override fun onDestroy() {
        Timber.i("onDestroy() called")
        super.onDestroy()
        stopAppManagerService()
    }

    /**
     * Shows a [LoadingFragment].
     */
    private fun showLoadingFragment() {
        Timber.i("showLoadingFragment() called")
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_holder, LoadingFragment())
                .commit()
    }

    /**
     * Check whether the [AppManagerFragment] is currently visible, and show it if it's not.
     */
    private fun ensureAppManagerVisible() {
        Timber.i("ensureAppManagerVisible() called")
        if (appManagerFragment == null) {
            appManagerFragment = AppManagerFragment()
            try {
                Timber.i("Trying to show AppManagerFragment")
                supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        .replace(R.id.fragment_holder, appManagerFragment!!)
                        .commit()
            } catch (e: IllegalStateException) {
                Timber.e(e)
            }
        }
    }

    /**
     * Start the App Manager service on the connected watch.
     */
    private fun startAppManagerService() {
        Timber.i("startAppManagerService() called")
        if (!isServiceRunning) {
            Timber.i("Trying to start App Manager service")
            isServiceRunning = true
            messageClient.sendMessage(watchId!!, References.START_SERVICE, null)
        }
    }

    /**
     * Stop the App Manager service on the connected watch.
     */
    private fun stopAppManagerService() {
        Timber.i("stopAppManagerService() called")
        if (isServiceRunning) {
            Timber.i("Trying to stop App Manager service")
            isServiceRunning = false
            messageClient.sendMessage(watchId!!, References.STOP_SERVICE, null)
        }
    }

    /**
     * Tell the user we couldn't connect to the watch, and close the activity.
     */
    private fun notifyWatchNotFound() {
        Timber.i("notifyWatchNotFound() called")
        Toast.makeText(this, getString(R.string.app_manager_unable_to_connect), Toast.LENGTH_LONG).show()
        finish()
    }

    companion object {
        const val EXTRA_WATCH_ID = "extra_watch_id"
        const val EXTRA_WATCH_NAME = "extra_watch_name"
    }
}
