package com.boswelja.devicemanager.appmanager.ui.info

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.appmanager.App
import com.boswelja.devicemanager.common.ui.activity.BaseToolbarActivity
import com.boswelja.devicemanager.databinding.ActivityAppInfoBinding

class AppInfoActivity : BaseToolbarActivity() {

    private val viewModel: AppInfoViewModel by viewModels()
    private val permissionDialogFragment by lazy { AppPermissionDialogFragment() }

    private lateinit var binding: ActivityAppInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAppInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding.toolbarLayout.toolbar, showTitle = false, showUpButton = true)

        viewModel.watchId = intent?.getStringExtra(EXTRA_WATCH_ID)
        val app = intent?.getSerializableExtra(EXTRA_APP_INFO) as App?
        viewModel.app = app

        app?.let {
            setupAppInfo(it)
            setupButtons(it)
            setupRequestedPermissions(it)
            setupPackageInfoViews(it)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Sets up the header view containing the app icon and name.
     * @param app The [App] to use data from.
     */
    private fun setupAppInfo(app: App) {
        binding.apply {
            app.icon?.bitmap?.let {
                appIcon.setImageBitmap(it)
            }
            appName.text = app.label
        }
    }

    /**
     * Sets up button views.
     * @param app The [App] to use data from.
     */
    private fun setupButtons(app: App) {
        binding.apply {
            openButton.isEnabled = app.hasLaunchActivity
            openButton.setOnClickListener {
                viewModel.sendOpenRequestMessage()
            }
            uninstallButton.isEnabled = !app.isSystemApp
            uninstallButton.setOnClickListener {
                viewModel.sendUninstallRequestMessage()
            }
        }
    }

    /**
     * Sets up the requested permissions view.
     * @param app The [App] to use data from.
     */
    private fun setupRequestedPermissions(app: App) {
        val requestsPermissions = app.requestedPermissions.isNotEmpty()
        binding.apply {
            permissionsInfo.topLine.text =
                getString(R.string.app_info_requested_permissions_title)
            permissionsInfo.bottomLine.text =
                if (requestsPermissions) {
                    val requestedPermissionCount = app.requestedPermissions.size
                    resources.getQuantityString(
                        R.plurals.app_info_requested_permissions_count,
                        requestedPermissionCount,
                        requestedPermissionCount
                    )
                } else {
                    getString(R.string.app_info_requested_permissions_none)
                }
            permissionsInfo.root.setOnClickListener {
                if (requestsPermissions) {
                    permissionDialogFragment.show(supportFragmentManager)
                }
            }
        }
    }

    /**
     * Sets up package info views.
     * @param app The [App] to use data from.
     */
    private fun setupPackageInfoViews(app: App) {
        binding.apply {
            appInstallTime.text = getString(
                R.string.app_info_first_installed_prefix,
                viewModel.formatDate(app.installTime)
            )
            appLastUpdatedTime.isVisible = app.installTime != app.lastUpdateTime
            appLastUpdatedTime.text = getString(
                R.string.app_info_last_updated_prefix,
                viewModel.formatDate(app.lastUpdateTime)
            )
            appVersionView.text = getString(R.string.app_info_version_prefix, app.version)
        }
    }

    companion object {
        const val EXTRA_APP_INFO = "extra_app_info"
        const val EXTRA_WATCH_ID = "extra_watch_id"
    }
}
