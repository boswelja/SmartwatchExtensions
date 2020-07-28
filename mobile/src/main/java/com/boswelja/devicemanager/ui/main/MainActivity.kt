/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.databinding.ActivityMainBinding
import com.boswelja.devicemanager.ui.base.BaseWatchPickerActivity
import com.boswelja.devicemanager.ui.changelog.ChangelogDialogFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import timber.log.Timber

class MainActivity : BaseWatchPickerActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupWatchPickerSpinner(binding.toolbarLayout.toolbar)
        binding.bottomNavigation.setupWithNavController(findNavController(R.id.nav_host_fragment))
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart() called")
        showChangelogIfNeeded()

        viewModel.messageCount.observe(this) {
            updateMessagesBadge(it)
        }
    }

    /**
     * Shows the changelog if [SHOW_CHANGELOG_KEY] is true.
     */
    private fun showChangelogIfNeeded() {
        if (sharedPreferences.getBoolean(SHOW_CHANGELOG_KEY, false)) {
            Timber.i("Showing changelog")
            ChangelogDialogFragment().show(supportFragmentManager)
        }
    }

    /**
     * Updates the message count badge on the [BottomNavigationView].
     */
    private fun updateMessagesBadge(messageCount: Int) {
        if (messageCount > 0) {
            binding.bottomNavigation.getOrCreateBadge(R.id.messageFragment).apply {
                number = messageCount
            }
        } else {
            binding.bottomNavigation.removeBadge(R.id.messageFragment)
        }
    }

    companion object {
        const val SHOW_CHANGELOG_KEY = "should_show_changelog"
    }
}
