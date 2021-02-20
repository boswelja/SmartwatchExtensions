/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.onboarding.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.activity.BaseToolbarActivity
import com.boswelja.devicemanager.databinding.ActivityOnboardingBinding
import com.boswelja.devicemanager.main.MainActivity
import com.boswelja.devicemanager.watchmanager.ui.register.RegisterWatchViewModel

class OnboardingActivity : BaseToolbarActivity() {

    private val registerWatchViewModel: RegisterWatchViewModel by viewModels()

    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
            .navController
    }

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding.toolbarLayout.toolbar)
        NavigationUI.setupActionBarWithNavController(this, navController)

        // When watch registration completes, head back to MainActivity
        registerWatchViewModel.onFinished.observe(this) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
