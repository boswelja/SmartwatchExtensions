/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.appmanager

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.appmanager.AppPackageInfo
import com.boswelja.devicemanager.common.appmanager.References.PACKAGE_ADDED
import com.boswelja.devicemanager.common.appmanager.References.PACKAGE_REMOVED
import com.boswelja.devicemanager.common.appmanager.References.PACKAGE_UPDATED
import com.boswelja.devicemanager.common.appmanager.References.REQUEST_OPEN_PACKAGE
import com.boswelja.devicemanager.common.appmanager.References.REQUEST_UNINSTALL_PACKAGE
import com.boswelja.devicemanager.common.recyclerview.adapter.ItemClickCallback
import com.boswelja.devicemanager.databinding.FragmentAppManagerBinding
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable

class AppManagerFragment : Fragment(), ItemClickCallback<AppPackageInfo> {

    private lateinit var messageClient: MessageClient
    private lateinit var activity: AppManagerActivity
    private lateinit var binding: FragmentAppManagerBinding
    private lateinit var appsAdapter: AppsAdapter

    private var allAppsCache: ArrayList<Pair<String, ArrayList<AppPackageInfo>>>? = null
    private var watchId: String? = null

    private val messageListener = MessageClient.OnMessageReceivedListener {
        when (it.path) {
            PACKAGE_ADDED -> {
                val appPackageInfo = AppPackageInfo.fromByteArray(it.data)
                handlePackageAdded(appPackageInfo)
            }
            PACKAGE_UPDATED -> {
                val appPackageInfo = AppPackageInfo.fromByteArray(it.data)
                handlePackageUpdated(appPackageInfo)
            }
            PACKAGE_REMOVED -> {
                val appPackageName = String(it.data, Charsets.UTF_8)
                handlePackageRemoved(appPackageName)
            }
        }
    }

    override fun onClick(item: AppPackageInfo) {
        launchAppInfoActivity(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        messageClient = Wearable.getMessageClient(requireContext())
        activity = getActivity() as AppManagerActivity

        watchId = activity.watchId
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAppManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appsAdapter = AppsAdapter(this)
        binding.appsRecyclerview.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = appsAdapter
        }
        if (allAppsCache != null) {
            setAllApps(allAppsCache!!)
        }
    }

    override fun onResume() {
        super.onResume()
        messageClient.addListener(messageListener)
    }

    override fun onPause() {
        super.onPause()
        messageClient.removeListener(messageListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            APP_INFO_ACTIVITY_REQUEST_CODE -> {
                activity.canStopAppManagerService = true
                when (resultCode) {
                    AppInfoActivity.RESULT_REQUEST_UNINSTALL -> {
                        val app = data?.extras?.getSerializable(AppInfoActivity.EXTRA_APP_INFO) as AppPackageInfo
                        sendUninstallRequestMessage(app)
                        activity.createSnackBar(getString(R.string.app_manager_continue_on_watch))
                    }
                    AppInfoActivity.RESULT_REQUEST_OPEN -> {
                        val app = data?.extras?.getSerializable(AppInfoActivity.EXTRA_APP_INFO) as AppPackageInfo
                        sendOpenRequestMessage(app)
                        activity.createSnackBar(getString(R.string.app_manager_continue_on_watch))
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Add a new [AppPackageInfo] to the [AppsAdapter] and notifies the user.
     * @param appInfo The [AppPackageInfo] to add.
     */
    private fun handlePackageAdded(appInfo: AppPackageInfo) {
        if (appInfo.isSystemApp) {
            appsAdapter.addItem(1, appInfo)
        } else {
            appsAdapter.addItem(0, appInfo)
        }
        activity.createSnackBar(getString(R.string.app_manager_installed_prefix, appInfo.packageLabel))
    }

    /**
     * Updates an [AppPackageInfo] in the [AppsAdapter] and notifies the user.
     * @param appInfo The [AppPackageInfo] to update.
     */
    private fun handlePackageUpdated(appInfo: AppPackageInfo) {
        appsAdapter.updateItem(appInfo)
        activity.createSnackBar(getString(R.string.app_manager_updated_prefix, appInfo.packageLabel))
    }

    /**
     * Removes an [AppPackageInfo] from the [AppsAdapter] and notifies the user.
     * @param appPackageName The package name of the [AppPackageInfo] to remove.
     */
    private fun handlePackageRemoved(appPackageName: String) {
        val removedApp = appsAdapter.removeByPackageName(appPackageName)
        if (removedApp != null) {
            activity.createSnackBar(
                getString(
                    R.string.app_manager_uninstalled_prefix, removedApp.packageLabel
                )
            )
        }
    }

    /**
     * Request uninstalling an app from the connected watch.
     * @param appPackageInfo The [AppPackageInfo] to request an uninstall for.
     */
    private fun sendUninstallRequestMessage(appPackageInfo: AppPackageInfo) {
        messageClient.sendMessage(
            watchId!!, REQUEST_UNINSTALL_PACKAGE,
            appPackageInfo.packageName.toByteArray(Charsets.UTF_8)
        )
    }

    /**
     * Request opening an app's launch activity on the connected watch.
     * @param appPackageInfo The [AppPackageInfo] to open the launch activity for.
     */
    private fun sendOpenRequestMessage(appPackageInfo: AppPackageInfo) {
        messageClient.sendMessage(
            watchId!!, REQUEST_OPEN_PACKAGE,
            appPackageInfo.packageName.toByteArray(Charsets.UTF_8)
        )
    }

    /**
     * Sets the list of apps to show in the [AppsAdapter].
     * @param apps The new list of [AppPackageInfo] to display.
     */
    fun setAllApps(apps: ArrayList<Pair<String, ArrayList<AppPackageInfo>>>) {
        allAppsCache = try {
            appsAdapter.setItems(apps)
            null
        } catch (e: Exception) {
            apps
        }
    }

    /**
     * Launches an [AppInfoActivity] for a given [AppPackageInfo].
     * @param appPackageInfo The [AppPackageInfo] object to pass on to the [AppInfoActivity].
     */
    private fun launchAppInfoActivity(appPackageInfo: AppPackageInfo) {
        activity.canStopAppManagerService = false
        val intent = Intent(context, AppInfoActivity::class.java)
        intent.putExtra(AppInfoActivity.EXTRA_APP_INFO, appPackageInfo)
        startActivityForResult(intent, APP_INFO_ACTIVITY_REQUEST_CODE)
    }

    companion object {
        private const val APP_INFO_ACTIVITY_REQUEST_CODE = 22668
    }
}
