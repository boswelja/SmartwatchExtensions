/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.watchsetup

import android.os.Bundle
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity

class WatchSetupActivity : BaseToolbarActivity() {

    private var useFirstFragmentAnimation = true

    override fun getContentViewId(): Int = R.layout.activity_watch_setup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(RESULT_NO_WATCH_ADDED)

        val shouldSkipWelcome = intent.getBooleanExtra(EXTRA_SKIP_WELCOME, false)
        if (shouldSkipWelcome) {
            startSetupFlow()
        } else {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            showWelcomeFragment()
        }
    }

    /**
     * Shows the [WelcomeFragment].
     */
    private fun showWelcomeFragment() {
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_holder, WelcomeFragment())
            setCustomAnimations(R.anim.fade_in, R.anim.slide_out_right)
        }.also {
            it.commit()
        }
        useFirstFragmentAnimation = false
    }

    /**
     * Shows the [WatchSetupFragment].
     */
    private fun showWatchSetupFragment() {
        supportActionBar?.title = getString(R.string.register_watch_toolbar_title)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_holder, WatchSetupFragment())
            if (useFirstFragmentAnimation) {
                setCustomAnimations(R.anim.fade_in, R.anim.slide_out_left)
                useFirstFragmentAnimation = false
            } else {
                setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }.also {
            it.commit()
        }
    }

    /**
     * Starts the app setup flow.
     */
    fun startSetupFlow() {
        showWatchSetupFragment()
    }

    companion object {
        const val RESULT_WATCH_ADDED = 1
        const val RESULT_NO_WATCH_ADDED = 0

        const val EXTRA_SKIP_WELCOME = "extra_skip_welcome"
    }
}
