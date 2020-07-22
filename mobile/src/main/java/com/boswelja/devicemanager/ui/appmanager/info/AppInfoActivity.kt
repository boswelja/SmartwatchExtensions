/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.appmanager.info

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.DataBindingUtil
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.appmanager.AppPackageInfo
import com.boswelja.devicemanager.databinding.ActivityAppInfoBinding
import com.boswelja.devicemanager.ui.appmanager.AppPermissionDialogFragment
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity

class AppInfoActivity : BaseToolbarActivity() {

    private val viewModel: AppInfoViewModel by viewModels()
    private val requestedPermissionsDialog by lazy {
        AppPermissionDialogFragment(viewModel.appInfo.value?.requestedPermissions!!)
    }

    private lateinit var binding: ActivityAppInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_app_info)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setupToolbar(binding.toolbarLayout.toolbar, showTitle = true, showUpButton = true)

        val appInfo = intent?.extras?.getSerializable(EXTRA_APP_INFO) as AppPackageInfo?
        viewModel.appInfo.postValue(appInfo)

        viewModel.appInfo.observe(this) {
            setupButtons(it)
            setupRequestedPermissions(it)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Sets click listeners and enabled states for all buttons.
     * @param appInfo The [AppPackageInfo] to use for data etc.
     */
    private fun setupButtons(appInfo: AppPackageInfo) {
        binding.openButton.setOnClickListener {
            Intent().apply {
                putExtra(EXTRA_APP_INFO, appInfo)
            }.also {
                setResult(RESULT_REQUEST_OPEN, it)
                finish()
            }
        }
        binding.uninstallButton.setOnClickListener {
            Intent().apply {
                putExtra(EXTRA_APP_INFO, appInfo)
            }.also {
                setResult(RESULT_REQUEST_UNINSTALL, it)
                finish()
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

    companion object {
        const val EXTRA_APP_INFO = "extra_app_info"

        const val RESULT_REQUEST_UNINSTALL = 718181
        const val RESULT_REQUEST_OPEN = 181817
    }
}
