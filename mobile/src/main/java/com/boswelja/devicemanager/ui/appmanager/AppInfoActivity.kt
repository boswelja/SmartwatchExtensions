/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
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
import com.boswelja.devicemanager.ui.base.BaseDayNightActivity
import java.text.SimpleDateFormat
import java.util.Locale

class AppInfoActivity : BaseDayNightActivity() {

    private lateinit var app: AppPackageInfo
    private lateinit var requestedPermissionsDialog: AppPermissionDialogFragment

    private lateinit var binding: ActivityAppInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_app_info)

        setSupportActionBar(binding.appbarLayout.findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        app = intent?.extras?.getSerializable(EXTRA_APP_INFO) as AppPackageInfo
        binding.appInfo = app

        if (!app.requestedPermissions.isNullOrEmpty()) {
            requestedPermissionsDialog = AppPermissionDialogFragment(app.requestedPermissions!!)
        }
        setAppIcon()
        setupButtons()
        setupRequestedPermissions()
        setInstallInfo()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setAppIcon() {
        binding.appIcon.setImageDrawable(Utils.getAppIcon(this, app.packageName))
    }

    private fun setupButtons() {
        binding.openButton.apply {
            isEnabled = app.hasLaunchActivity

            if (isEnabled) {
                setOnClickListener {
                    Intent().apply {
                        putExtra(EXTRA_APP_INFO, app)
                    }.also {
                        setResult(RESULT_REQUEST_OPEN, it)
                        finish()
                    }
                }
            }
        }
        binding.uninstallButton.apply {
            isEnabled = (app.packageName != packageName && !app.isSystemApp)

            if (isEnabled) {
                setOnClickListener {
                    Intent().apply {
                        putExtra(EXTRA_APP_INFO, app)
                    }.also {
                        setResult(RESULT_REQUEST_UNINSTALL, it)
                        finish()
                    }
                }
            }
        }
    }

    private fun setupRequestedPermissions() {
        val permissionItemView = binding.permissionsInfo
        val requestsNoPermissions = !app.requestedPermissions.isNullOrEmpty()
        permissionItemView.findViewById<AppCompatTextView>(R.id.top_line).apply {
            text = getString(R.string.app_info_requested_permissions_title)
        }
        permissionItemView.findViewById<AppCompatTextView>(R.id.bottom_line).apply {
            text = if (requestsNoPermissions) {
                val requestedPermissionCount = app.requestedPermissions!!.size
                resources.getQuantityString(R.plurals.app_info_requested_permissions_count, requestedPermissionCount, requestedPermissionCount)
            } else {
                getString(R.string.app_info_requested_permissions_none)
            }
        }
        permissionItemView.setOnClickListener {
            if (app.requestedPermissions!!.isNotEmpty()) {
                requestedPermissionsDialog.show(supportFragmentManager, "RequestedPermissionsDialog")
            }
        }
    }

    private fun setInstallInfo() {
        val dateFormat = SimpleDateFormat("EE, dd MMM yyyy, h:mm aa", Locale.getDefault())
        binding.appInstallTime.apply {
            if (app.isSystemApp) {
                visibility = View.GONE
            } else {
                text = getString(R.string.app_info_first_installed_prefix).format(dateFormat.format(app.installTime))
            }
        }
        binding.appLastUpdatedTime.apply {
            if (app.installTime == app.lastUpdateTime && !app.isSystemApp) {
                visibility = View.GONE
            } else {
                text = getString(R.string.app_info_last_updated_prefix).format(dateFormat.format(app.lastUpdateTime))
            }
        }
    }

    companion object {
        const val EXTRA_APP_INFO = "extra_app_info"

        const val RESULT_REQUEST_UNINSTALL = 718181
        const val RESULT_REQUEST_OPEN = 181817
    }
}
