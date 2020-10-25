/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.BaseWatchPickerActivity
import com.boswelja.devicemanager.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.updatePriority
import timber.log.Timber

class MainActivity : BaseWatchPickerActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupWatchPickerSpinner(binding.toolbarLayout.toolbar)
        binding.bottomNavigation.setupWithNavController(
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
                .navController)
        ensureAppUpdated()
    }

    override fun onStart() {
        super.onStart()
        viewModel.messageCount.observe(this) { updateMessagesBadge(it) }
    }

    /** Updates the message count badge on the [BottomNavigationView]. */
    private fun updateMessagesBadge(messageCount: Int) {
        if (messageCount > 0) {
            binding.bottomNavigation.getOrCreateBadge(R.id.messageFragment).apply {
                number = messageCount
            }
        } else {
            binding.bottomNavigation.removeBadge(R.id.messageFragment)
        }
    }

    private fun ensureAppUpdated() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.appUpdateInfo.addOnCompleteListener {
            if (it.isSuccessful) {
                val appUpdateInfo = it.result
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    appUpdateInfo.updatePriority <= LOW_PRIORITY_UPDATE) {
                    if (appUpdateInfo.updatePriority < HIGH_PRIORITY_UPDATE &&
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo, AppUpdateType.FLEXIBLE, this, 0)
                    } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo, AppUpdateType.IMMEDIATE, this, 0)
                    }
                }
            } else {
                Timber.w("Failed to check for app updates")
            }
        }
    }

    companion object {
        private const val LOW_PRIORITY_UPDATE = 2
        private const val HIGH_PRIORITY_UPDATE = 5
    }
}
