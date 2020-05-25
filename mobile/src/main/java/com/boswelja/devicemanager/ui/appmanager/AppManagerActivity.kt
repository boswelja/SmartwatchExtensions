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
import com.boswelja.devicemanager.common.appmanager.AppPackageInfo
import com.boswelja.devicemanager.common.appmanager.AppPackageInfoList
import com.boswelja.devicemanager.common.appmanager.References
import com.boswelja.devicemanager.databinding.ActivityAppManagerBinding
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity
import com.boswelja.devicemanager.ui.common.LoadingFragment
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import java.io.InvalidClassException
import timber.log.Timber

class AppManagerActivity : BaseToolbarActivity() {

    private lateinit var messageClient: MessageClient
    private lateinit var binding: ActivityAppManagerBinding

    private var appManagerFragment: AppManagerFragment? = null
    private var isServiceRunning: Boolean = false

    var canStopAppManagerService: Boolean = true
    var watchId: String? = null

    private val messageListener = MessageClient.OnMessageReceivedListener {
        Timber.i("Received a message")
        when (it.path) {
            References.GET_ALL_PACKAGES -> {
                Timber.i("Updating app list")
                try {
                    val appPackageInfoList = AppPackageInfoList.fromByteArray(it.data)
                    val allApps = separateAppListToSections(appPackageInfoList)
                    ensureAppManagerVisible()
                    appManagerFragment!!.setAllApps(allApps)
                } catch (e: InvalidClassException) {
                    createSnackBar(getString(R.string.app_manager_version_mismatch))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("onCreate() called")

        watchId = intent?.getStringExtra(EXTRA_WATCH_ID)
        if (watchId.isNullOrEmpty()) {
            // TODO Load in an error fragment instead of just terminating
            notifyWatchNotFound()
            return
        }

        binding = ActivityAppManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(
                binding.toolbarLayout.toolbar,
                showTitle = true,
                showUpButton = true,
                toolbarSubtitle = getString(
                        R.string.app_manager_activity_subtitle,
                        intent.getStringExtra(EXTRA_WATCH_NAME)))

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
     * Converts an [AppPackageInfoList] into an [ArrayList] that can be used by [AppsAdapter].
     * @param appPackageInfoList The [AppPackageInfoList] to convert.
     * @return The newly created [ArrayList].
     */
    private fun separateAppListToSections(appPackageInfoList: AppPackageInfoList):
            ArrayList<Pair<String, ArrayList<AppPackageInfo>>> {
        val data = ArrayList<Pair<String, ArrayList<AppPackageInfo>>>()
        val userApps = ArrayList<AppPackageInfo>()
        userApps.addAll(appPackageInfoList.filterNot { it.isSystemApp })
        Pair(getString(R.string.app_manager_section_user_apps), userApps).also {
            data.add(it)
        }

        val systemApps = ArrayList<AppPackageInfo>()
        systemApps.addAll(appPackageInfoList.filter { it.isSystemApp })
        Pair(getString(R.string.app_manager_section_system_apps), systemApps).also {
            data.add(it)
        }
        return data
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
