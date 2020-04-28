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
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.DataBindingUtil
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.appmanager.AppPackageInfo
import com.boswelja.devicemanager.databinding.ActivityAppInfoBinding
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity
import java.text.SimpleDateFormat
import java.util.Locale

class AppInfoActivity : BaseToolbarActivity() {

    private lateinit var requestedPermissionsDialog: AppPermissionDialogFragment

    private lateinit var binding: ActivityAppInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_app_info)

        setupToolbar(binding.toolbarLayout.toolbar, showTitle = true, showUpButton = true)

        val appInfo = intent?.extras?.getSerializable(EXTRA_APP_INFO) as AppPackageInfo
        binding.appInfo = appInfo

        if (!appInfo.requestedPermissions.isNullOrEmpty()) {
            requestedPermissionsDialog = AppPermissionDialogFragment(appInfo.requestedPermissions!!)
        }
        setAppIcon(appInfo)
        setupButtons(appInfo)
        setupRequestedPermissions(appInfo)
        setInstallInfo(appInfo)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Gets an icon for a given [AppPackageInfo] object and shows it in the UI.
     * @param appInfo The [AppPackageInfo] to get an icon for.
     */
    private fun setAppIcon(appInfo: AppPackageInfo) {
        binding.appIcon.setImageDrawable(Utils.getAppIcon(this, appInfo.packageName))
    }

    /**
     * Checks whether a given [AppPackageInfo] can be opened.
     * @param appInfo The [AppPackageInfo] object to check against.
     * @return true if we can open the app, false otherwise.
     */
    private fun canOpenApp(appInfo: AppPackageInfo): Boolean = appInfo.hasLaunchActivity

    /**
     * Checks whether a given [AppPackageInfo] can be uninstalled.
     * @param appInfo The [AppPackageInfo] object to check against.
     * @return true if we can uninstall the app, false otherwise
     */
    private fun canUninstallApp(appInfo: AppPackageInfo): Boolean =
            (appInfo.packageName != packageName) && !appInfo.isSystemApp

    /**
     * Sets click listeners and enabled states for all buttons.
     * @param appInfo The [AppPackageInfo] to use for data etc.
     */
    private fun setupButtons(appInfo: AppPackageInfo) {
        binding.openButton.apply {
            if (canOpenApp(appInfo)) {
                isEnabled = true
                setOnClickListener {
                    Intent().apply {
                        putExtra(EXTRA_APP_INFO, appInfo)
                    }.also {
                        setResult(RESULT_REQUEST_OPEN, it)
                        finish()
                    }
                }
            }
        }
        binding.uninstallButton.apply {
            if (canUninstallApp(appInfo)) {
                isEnabled = true
                setOnClickListener {
                    Intent().apply {
                        putExtra(EXTRA_APP_INFO, appInfo)
                    }.also {
                        setResult(RESULT_REQUEST_UNINSTALL, it)
                        finish()
                    }
                }
            }
        }
    }

    /**
     * Sets up the requested permissions view.
     * @param appInfo The [AppPackageInfo] to use for data etc.
     */
    private fun setupRequestedPermissions(appInfo: AppPackageInfo) {
        val requestsNoPermissions = !appInfo.requestedPermissions.isNullOrEmpty()
        binding.apply {
            permissionsInfo.findViewById<AppCompatTextView>(R.id.top_line).text =
                    getString(R.string.app_info_requested_permissions_title)
            permissionsInfo.findViewById<AppCompatTextView>(R.id.bottom_line).text =
                    if (requestsNoPermissions) {
                        val requestedPermissionCount = appInfo.requestedPermissions!!.size
                        resources.getQuantityString(
                                R.plurals.app_info_requested_permissions_count,
                                requestedPermissionCount, requestedPermissionCount)
                    } else {
                        getString(R.string.app_info_requested_permissions_none)
                    }
            permissionsInfo.setOnClickListener {
                if (appInfo.requestedPermissions!!.isNotEmpty()) {
                    requestedPermissionsDialog.show(supportFragmentManager)
                }
            }
        }
    }

    /**
     * Checks whether we should show the app's last update time.
     * @param appInfo The [AppPackageInfo] object to check against.
     * @return true if we should show last update time, false otherwise.
     */
    private fun shouldShowLastUpdateTime(appInfo: AppPackageInfo): Boolean =
            appInfo.installTime == appInfo.lastUpdateTime && !appInfo.isSystemApp

    /**
     * Sets the install/update info in the UI.
     * @param appInfo The [AppPackageInfo] object to get data from.
     */
    private fun setInstallInfo(appInfo: AppPackageInfo) {
        val dateFormat = SimpleDateFormat("EE, dd MMM yyyy, h:mm aa", Locale.getDefault())
        binding.appInstallTime.apply {
            if (appInfo.isSystemApp) {
                visibility = View.GONE
            } else {
                text = getString(R.string.app_info_first_installed_prefix).format(dateFormat.format(appInfo.installTime))
            }
        }
        binding.appLastUpdatedTime.apply {
            if (shouldShowLastUpdateTime(appInfo)) {
                visibility = View.GONE
            } else {
                text = getString(R.string.app_info_last_updated_prefix).format(dateFormat.format(appInfo.lastUpdateTime))
            }
        }
    }

    companion object {
        const val EXTRA_APP_INFO = "extra_app_info"

        const val RESULT_REQUEST_UNINSTALL = 718181
        const val RESULT_REQUEST_OPEN = 181817
    }
}
