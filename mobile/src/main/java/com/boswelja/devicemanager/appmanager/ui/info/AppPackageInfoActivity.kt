/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.appmanager.ui.info

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.DataBindingUtil
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.appmanager.App
import com.boswelja.devicemanager.common.ui.activity.BaseToolbarActivity
import com.boswelja.devicemanager.databinding.ActivityAppInfoBinding

class AppPackageInfoActivity : BaseToolbarActivity() {

    private val viewModel: AppPackageInfoViewModel by viewModels()
    private lateinit var binding: ActivityAppInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_app_info)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setupToolbar(binding.toolbarLayout.toolbar, showTitle = true, showUpButton = true)

        val watchId = intent?.getStringExtra(EXTRA_WATCH_ID)
        viewModel.watchId = watchId
        val appInfo = intent?.getSerializableExtra(EXTRA_APP_INFO) as App?
        viewModel.appInfo.postValue(appInfo)

        viewModel.appInfo.observe(this) { setupRequestedPermissions(it) }
        viewModel.finishActivity.observe(this) { if (it) finish() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Sets up the requested permissions view.
     * @param appInfo The [App] to use for data etc.
     */
    private fun setupRequestedPermissions(appInfo: App) {
        val requestsPermissions = !appInfo.requestedPermissions.isNullOrEmpty()
        binding.apply {
            permissionsInfo.findViewById<AppCompatTextView>(R.id.top_line).text =
                getString(R.string.app_info_requested_permissions_title)
            permissionsInfo.findViewById<AppCompatTextView>(R.id.bottom_line).text =
                if (requestsPermissions) {
                    val requestedPermissionCount = appInfo.requestedPermissions!!.size
                    resources.getQuantityString(
                        R.plurals.app_info_requested_permissions_count,
                        requestedPermissionCount,
                        requestedPermissionCount
                    )
                } else {
                    getString(R.string.app_info_requested_permissions_none)
                }
            permissionsInfo.setOnClickListener {
                if (requestsPermissions) {
                    AppPermissionDialogFragment(appInfo.requestedPermissions!!)
                        .show(supportFragmentManager)
                }
            }
        }
    }

    companion object {
        const val EXTRA_APP_INFO = "extra_app_info"
        const val EXTRA_WATCH_ID = "extra_watch_id"
    }
}
