/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.appmanager.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.appmanager.ui.adapter.AppsAdapter
import com.boswelja.devicemanager.appmanager.ui.adapter.HeaderAdapter
import com.boswelja.devicemanager.appmanager.ui.info.AppPackageInfoActivity
import com.boswelja.devicemanager.common.appmanager.App
import com.boswelja.devicemanager.databinding.FragmentAppManagerBinding
import timber.log.Timber

class AppManagerFragment : Fragment() {

    private val viewModel: AppManagerViewModel by activityViewModels()
    private val userAppsAdapter by lazy { AppsAdapter(onAppClick) }
    private val systemAppsAdapter by lazy { AppsAdapter(onAppClick) }

    private val onAppClick = { app: App ->
        launchAppInfoActivity(app)
    }

    private lateinit var binding: FragmentAppManagerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAppManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.appsRecyclerview.adapter = ConcatAdapter(
            HeaderAdapter(getString(R.string.app_manager_section_user_apps)),
            userAppsAdapter,
            HeaderAdapter(getString(R.string.app_manager_section_system_apps)),
            systemAppsAdapter
        )
        viewModel.apps.observe(viewLifecycleOwner) {
            it?.let { allApps ->
                updateAppsInAdapter(allApps)
            }
        }
    }

    /**
     * Separate a given [List] of [App]s into sections, and submit the new lists to the appropriate
     * adapter.
     * @param allApps The [List] of [App]s to separate.
     */
    private fun updateAppsInAdapter(allApps: List<App>) {
        Timber.d("updateAppsInAdapter($allApps) called")
        val userApps = ArrayList<App>()
        val systemApps = ArrayList<App>()
        allApps.forEach { app ->
            if (app.isSystemApp) systemApps.add(app)
            else userApps.add(app)
        }
        userAppsAdapter.submitList(userApps)
        systemAppsAdapter.submitList(systemApps)
    }

    /**
     * Launches an [AppPackageInfoActivity] for a given [App].
     * @param app The [App] object to pass on to the [AppPackageInfoActivity].
     */
    private fun launchAppInfoActivity(app: App) {
        Timber.d("launchAppInfoActivity($app) called")
        viewModel.canStopAppManagerService = false
        Intent(context, AppPackageInfoActivity::class.java)
            .apply {
                putExtra(AppPackageInfoActivity.EXTRA_APP_INFO, app)
                // putExtra(AppPackageInfoActivity.EXTRA_WATCH_ID, viewModel.watchId)
            }
            .also { startActivity(it) }
    }
}
