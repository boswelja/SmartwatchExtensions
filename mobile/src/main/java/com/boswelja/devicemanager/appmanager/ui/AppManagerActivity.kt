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
import androidx.fragment.app.commit
import androidx.navigation.navArgs
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.appmanager.State
import com.boswelja.devicemanager.common.ui.activity.BaseToolbarActivity
import com.boswelja.devicemanager.common.ui.fragment.LoadingFragment
import com.boswelja.devicemanager.databinding.ActivityAppManagerBinding
import timber.log.Timber

class AppManagerActivity : BaseToolbarActivity() {

    private var shouldAnimateTransitions = false

    private val args: AppManagerActivityArgs by navArgs()
    private val viewModel: AppManagerViewModel by viewModels()

    private val watchServiceLifecycleObserver by lazy { WatchServiceLifecycleObserver(viewModel) }
    private val loadingFragment = LoadingFragment()
    private val appListFragment by lazy { AppListFragment() }

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

        viewModel.state.observe(this) {
            when (it) {
                State.CONNECTING, State.LOADING_APPS -> showLoadingFragment()
                State.READY -> showAppManagerFragment()
                State.DISCONNECTED -> notifyDisconnected()
                State.ERROR -> TODO()
                else -> Timber.e("App Manager state is null")
            }
        }

        viewModel.progress.observe(this) {
            loadingFragment.progress.postValue(it)
        }

        lifecycle.addObserver(watchServiceLifecycleObserver)
    }

    /** Shows a [LoadingFragment]. */
    private fun showLoadingFragment() {
        Timber.i("showLoadingFragment() called")
        supportFragmentManager.commit {
            if (shouldAnimateTransitions) {
                setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            } else {
                shouldAnimateTransitions = true
            }
            replace(R.id.fragment_holder, loadingFragment)
        }
    }

    private fun notifyDisconnected() {
        // TODO show a disconnected indicator in App List fragment
    }

    /** Shows the [AppListFragment]. */
    private fun showAppManagerFragment() {
        Timber.i("showAppManagerFragment() called")
        supportFragmentManager.commit {
            if (shouldAnimateTransitions) {
                setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            } else {
                shouldAnimateTransitions = true
            }
            replace(R.id.fragment_holder, appListFragment)
        }
    }
}
