/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.dndsync.ui.helper

import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.databinding.ActivityDndSyncHelperBinding
import com.boswelja.devicemanager.common.ui.BaseToolbarActivity
import timber.log.Timber

class DnDSyncHelperActivity : BaseToolbarActivity() {

    private lateinit var binding: ActivityDndSyncHelperBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate() called")

        binding = ActivityDndSyncHelperBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar(binding.toolbarLayout.toolbar, showUpButton = true)
        binding.toolbarLayout.toolbar.setupWithNavController(findNavController(R.id.nav_host_fragment))
    }
}
