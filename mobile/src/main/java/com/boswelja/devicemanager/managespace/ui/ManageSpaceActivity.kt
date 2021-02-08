/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.managespace.ui

import android.os.Bundle
import androidx.activity.viewModels
import com.boswelja.devicemanager.common.ui.BaseToolbarActivity
import com.boswelja.devicemanager.databinding.ActivityManageSpaceBinding

class ManageSpaceActivity : BaseToolbarActivity() {

    private lateinit var binding: ActivityManageSpaceBinding

    private val viewModel: ManageSpaceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManageSpaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding.toolbarLayout.toolbar, showTitle = true, showUpButton = true)
        setupButtons()
        viewModel
    }

    /** Initialise all the buttons we need. */
    private fun setupButtons() {
        binding.apply {
            clearCacheButton.setOnClickListener { clearCache() }
            resetSettingsButton.setOnClickListener { doSettingsReset() }
            resetAppButton.setOnClickListener { doFullReset() }
        }
    }

    /** Attempts to clear all of Wearable Extension's cache. */
    private fun clearCache() {
        ClearCacheBottomSheet()
            .show(supportFragmentManager, ClearCacheBottomSheet::class.simpleName)
    }

    /** Attempts to reset settings for each watch registered with Wearable Extensions. */
    private fun doSettingsReset() {
        ResetSettingsBottomSheet().show(supportFragmentManager, "ResetSettingsBottomSheet")
    }

    /** Attempts a full reset of Wearable Extensions. */
    private fun doFullReset() {
        ResetAppBottomSheet().show(supportFragmentManager, "ResetAppBottomSheet")
    }

    companion object {
        private const val DATABASE_COUNT = 3
        private const val PREFERENCE_STORE_COUNT = 1
    }
}
