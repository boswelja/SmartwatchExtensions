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
import com.boswelja.devicemanager.appmanager.ui.adapter.AppsAdapter
import com.boswelja.devicemanager.appmanager.ui.adapter.Item
import com.boswelja.devicemanager.appmanager.ui.info.AppPackageInfoActivity
import com.boswelja.devicemanager.common.appmanager.AppPackageInfo
import com.boswelja.devicemanager.common.recyclerview.adapter.ItemClickCallback
import com.boswelja.devicemanager.databinding.FragmentAppManagerBinding

class AppManagerFragment : Fragment(), ItemClickCallback<Item> {

    private val viewModel: AppManagerViewModel by activityViewModels()
    private val appsAdapter = AppsAdapter(this)

    private lateinit var binding: FragmentAppManagerBinding

    override fun onClick(item: Item) {
        if (item is Item.App) {
            viewModel.getAppDetails(item)?.let { launchAppInfoActivity(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAppManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.appsRecyclerview.adapter = appsAdapter
        viewModel.adapterList.observe(viewLifecycleOwner) {
            it?.let { allApps -> appsAdapter.submitList(allApps) }
        }
    }

    /**
     * Launches an [AppPackageInfoActivity] for a given [AppPackageInfo].
     * @param appPackageInfo The [AppPackageInfo] object to pass on to the [AppPackageInfoActivity].
     */
    private fun launchAppInfoActivity(appPackageInfo: AppPackageInfo) {
        viewModel.canStopAppManagerService = false
        Intent(context, AppPackageInfoActivity::class.java)
            .apply {
                putExtra(AppPackageInfoActivity.EXTRA_APP_INFO, appPackageInfo)
                putExtra(AppPackageInfoActivity.EXTRA_WATCH_ID, viewModel.watchId)
            }
            .also { startActivity(it) }
    }
}
