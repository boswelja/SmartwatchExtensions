/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.setup.ui

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.BaseToolbarActivity
import com.boswelja.devicemanager.databinding.ActivitySetupBinding

class SetupActivity : BaseToolbarActivity() {

    private val skippedWelcome by lazy { intent.getBooleanExtra(EXTRA_SKIP_WELCOME, false) }
    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
            .navController
    }

    private lateinit var binding: ActivitySetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding.toolbarLayout.toolbar)
        NavigationUI.setupActionBarWithNavController(this, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.setDisplayShowTitleEnabled(destination.id == R.id.watchSetupFragment)
        }

        if (skippedWelcome) {
            navController.navigate(R.id.start_setupFragment)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return if (skippedWelcome) {
            finish()
            true
        } else super.onSupportNavigateUp()
    }

    companion object {
        const val EXTRA_SKIP_WELCOME = "extra_skip_welcome"
    }
}
