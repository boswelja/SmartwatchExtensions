/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.appmanager.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.navArgs
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.appmanager.State
import com.boswelja.devicemanager.common.ui.activity.BaseToolbarActivity
import com.boswelja.devicemanager.common.ui.fragment.LoadingFragment
import com.boswelja.devicemanager.databinding.ActivityAppManagerBinding
import timber.log.Timber

class AppManagerActivity : BaseToolbarActivity() {

    private val args: AppManagerActivityArgs by navArgs()
    private val viewModel: AppManagerViewModel by viewModels()

    private val watchServiceLifecycleObserver by lazy { WatchServiceLifecycleObserver(viewModel) }

    private lateinit var binding: ActivityAppManagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("onCreate() called")

        binding = ActivityAppManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(
            binding.toolbarLayout.toolbar,
            showTitle = true,
            showUpButton = true,
            toolbarSubtitle = getString(R.string.app_manager_activity_subtitle, args.watchName)
        )

        viewModel.watchId = args.watchId

        viewModel.state.observe(this) {
            when (it) {
                State.DISCONNECTED, State.CONNECTING, State.LOADING_APPS -> showLoadingFragment()
                State.READY -> showAppManagerFragment()
                State.ERROR -> TODO()
                else -> Timber.e("App Manager state is null")
            }
        }
        lifecycle.addObserver(watchServiceLifecycleObserver)
    }

    /** Shows a [LoadingFragment]. */
    private fun showLoadingFragment() {
        Timber.i("showLoadingFragment() called")
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_holder, LoadingFragment())
            .commit()
    }

    /** Shows the [AppManagerFragment]. */
    private fun showAppManagerFragment() {
        Timber.i("showAppManagerFragment() called")
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .replace(R.id.fragment_holder, AppManagerFragment())
            .commit()
    }
}
